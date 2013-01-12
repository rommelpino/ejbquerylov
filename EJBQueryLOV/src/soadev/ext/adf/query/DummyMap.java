package soadev.ext.adf.query;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

// abstract class used by java server faces to pass parameter to a method as map key
public abstract class DummyMap implements Map {
        public Collection values() {return null;}
        public Object put(Object key, Object value) {return null;}
        public Set keySet() {return null;}
        public boolean isEmpty() {return false;}
        public int size() {return 0;}
        public void putAll(Map t) {}
        public void clear() {}
        public boolean containsValue(Object value) {return false;}
        public Object remove(Object key) {return null;  }
        public boolean containsKey(Object key) {return false;}
        public Set entrySet() {return null;}

        // subclasses should override this method call their method with obj as the parameter
        public abstract Object get(Object obj);
}