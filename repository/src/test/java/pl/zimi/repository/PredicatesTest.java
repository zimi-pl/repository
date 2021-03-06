package pl.zimi.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.zimi.repository.contract.Foo;
import pl.zimi.repository.contract.SFoo;

public class PredicatesTest {

    @Test
    void equal() {
        final Filter predicate = Predicates.eq(SFoo.foo.abc, "value");

        final Foo abc = Foo.builder().abc("value").build();
        Assertions.assertTrue(predicate.test(abc));

        Assertions.assertEquals("abc EQUAL value", predicate.describe());
    }

    @Test
    void equalWithNegativeResult() {
        final Filter predicate = Predicates.eq(SFoo.foo.abc, "value");

        final Foo foo = Foo.builder().abc("other").build();
        Assertions.assertFalse(predicate.test(foo));
        Assertions.assertEquals("abc EQUAL value", predicate.describe());
    }

    @Test
    void regex() {
        final Filter predicate = Predicates.regex(SFoo.foo.abc, "^[a-z]+$");

        final Foo foo = Foo.builder().abc("other").build();
        Assertions.assertTrue(predicate.test(foo));
        Assertions.assertEquals("abc REGEX ^[a-z]+$", predicate.describe());
    }


    @Test
    void regexContains() {
        final Filter predicate = Predicates.regex(SFoo.foo.abc, "the");

        final Foo foo = Foo.builder().abc("other").build();
        Assertions.assertTrue(predicate.test(foo));
        Assertions.assertEquals("abc REGEX the", predicate.describe());
    }

    @Test
    void regexContainsFails() {
        final Filter predicate = Predicates.regex(SFoo.foo.abc, "some");

        final Foo foo = Foo.builder().abc("other").build();
        Assertions.assertFalse(predicate.test(foo));
        Assertions.assertEquals("abc REGEX some", predicate.describe());
    }

    @Test
    void regexFailsForNull() {
        final Filter predicate = Predicates.regex(SFoo.foo.abc, "some");

        final Foo foo = Foo.builder().abc(null).build();
        Assertions.assertFalse(predicate.test(foo));
        Assertions.assertEquals("abc REGEX some", predicate.describe());
    }

    @Test
    void descriptiveComparator() {
        final Sort asc = Comparators.asc(SFoo.foo.abc);
        Assertions.assertEquals("abc", asc.getPath());
        Assertions.assertEquals(Direction.NATURAL, asc.getDirection());
        Assertions.assertEquals("abc NATURAL", asc.describe());
    }

}
