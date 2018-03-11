package util.practice.chapter4;

import java.util.Collections;
import java.util.Map;
import util.concurrent.ConcurrentHashMap;

/**
 * 该类线程安全的前提是不需要有效值施加任何约束
 *
 * @author yangqc 2016年8月29日
 */
public class PublishingVehicleTracker {
    private final Map<String, SafePoint> locations;
    private final Map<String, SafePoint> unmodifiableMap;

    public PublishingVehicleTracker(Map<String, SafePoint> locations) {
        this.locations = new ConcurrentHashMap<>(locations);
        this.unmodifiableMap = Collections.unmodifiableMap(this.locations);
    }

    public Map<String, SafePoint> getLocations() {
        return unmodifiableMap;
    }

    public SafePoint getLocation(String id) {
        return locations.get(id);
    }

    public void setLocation(String id, int x, int y) {
        if (!locations.containsKey(id)) {
            throw new IllegalArgumentException("错误id!");
        }
        locations.get(id).set(x, y);
    }
}
