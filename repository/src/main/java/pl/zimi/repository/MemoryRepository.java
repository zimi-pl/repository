package pl.zimi.repository;

import com.google.gson.Gson;
import pl.zimi.repository.annotation.Descriptor;
import pl.zimi.repository.contract.Contract;
import pl.zimi.repository.contract.OptimisticLockException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MemoryRepository<T> implements Repository<T> {

    private final Gson gson = new Gson();
    private final Map<Object, T> source = new HashMap<>();
    private final Contract<T> contract;
    private final AtomicInteger idCounter = new AtomicInteger(0);
    private final Descriptor versionDescriptor;

    public MemoryRepository(final Contract<T> contract) {
        this.contract = contract;
        this.versionDescriptor = contract.getVersion();
    }

    private T deepCopy(final T toCopy) {
        final Class<T> type = (Class<T>) toCopy.getClass();
        return gson.fromJson(gson.toJson(toCopy), type);
    }

    @Override
    public T save(T entity) {
        final T copied = deepCopy(entity);
        if (contract.getId() != null && Manipulator.get(copied, contract.getId()).getObject() == null) {
            Manipulator.set(copied, contract.getId(), Integer.toString(idCounter.getAndIncrement()));
            if (versionDescriptor != null) {
                Manipulator.set(copied, versionDescriptor, 0);
            }
        } else if (contract.getId() != null && versionDescriptor != null) {
            final var previousVersion = Manipulator.get(entity, versionDescriptor).getObject();
            final var id = Manipulator.get(entity, contract.getId()).getObject();
            final var currentEntity = source.get(id);
            if (currentEntity != null) {
                final var dbVersion = Manipulator.get(currentEntity, versionDescriptor).getObject();
                if (Objects.equals(previousVersion, dbVersion)) {
                    Manipulator.set(copied, versionDescriptor, ((Integer) previousVersion) + 1);
                } else {
                    throw new OptimisticLockException("Given version: " + previousVersion + ", db version: " + dbVersion);
                }
            } else {
                throw new OptimisticLockException("Given version: " + previousVersion + ", db version: null");
            }
        }
        final var id = contract.getId() != null ? Manipulator.get(copied, contract.getId()).getObject() : UUID.randomUUID().toString();
        source.put(id, copied);
        return deepCopy(copied);
    }

    @Override
    public List<T> find(final Filter filter, final Sort comparator, final LimitOffset limit) {
        final Stream<T> streamed = source.values().stream();
        final Stream<T> filtered = filter != null ? streamed.filter(filter::test) : streamed;
        final Stream<T> sorted = comparator != null ? filtered.sorted(comparator::compare) : filtered;
        final Stream<T> skipped = limit != null && limit.getOffset() != null ? sorted.skip(limit.getOffset()) : sorted;
        final Stream<T> limited = limit != null && limit.getLimit() != null ? skipped.limit(limit.getLimit()) : skipped;
        return limited
                .map(this::deepCopy)
                .collect(Collectors.toList());
    }

    @Override
    public List<T> findAll() {
        return find(null, null, null);
    }

    @Override
    public List<T> deleteAll(Predicate<T> predicate) {
        return null;
    }


}
