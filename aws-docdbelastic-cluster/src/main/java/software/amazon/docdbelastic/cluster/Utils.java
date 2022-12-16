package software.amazon.docdbelastic.cluster;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class Utils {
    static <T> Stream<T> streamOfOrEmpty(Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }
}
