package pl.zimi.repository.query;

import pl.zimi.repository.manipulation.Manipulator;
import pl.zimi.repository.manipulation.Value;
import pl.zimi.repository.annotation.Descriptor;

import java.util.Comparator;

public class Sorter {

    private final Descriptor path;
    private final Direction direction;

    public Sorter(Descriptor path, final Direction direction) {
        this.path = path;
        this.direction = direction;
    }

    public String getPath() {
        return path.getPath();
    }

    public Direction getDirection() {
        return direction;
    }

    public String describe() {
        return getPath() + " " + direction;
    }

    public int compare(Object o1, Object o2) {
        final Value v1 = Manipulator.get(o1, path);
        final Value v2 = Manipulator.get(o2, path);
        return direction.getOrder() * Comparator.<Comparable>naturalOrder().compare(v1, v2);
    }
}
