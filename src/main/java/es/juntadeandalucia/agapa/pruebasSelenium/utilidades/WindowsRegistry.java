package es.juntadeandalucia.agapa.pruebasSelenium.utilidades;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import lombok.extern.slf4j.Slf4j;


/**
 * Class WindowsRegistry.
 *
 * @author AGAPA
 */
@Slf4j
public class WindowsRegistry {

   /** La constante HKEY_CURRENT_USER. */
   public static final int                     HKEY_CURRENT_USER  = 0x80000001;

   /** La constante HKEY_LOCAL_MACHINE. */
   public static final int                     HKEY_LOCAL_MACHINE = 0x80000002;

   /** La constante REG_SUCCESS. */
   public static final int                     REG_SUCCESS        = 0;

   /** La constante REG_NOTFOUND. */
   public static final int                     REG_NOTFOUND       = 2;

   /** La constante REG_ACCESSDENIED. */
   public static final int                     REG_ACCESSDENIED   = 5;

   /** La constante KEY_ALL_ACCESS. */
   private static final int                    KEY_ALL_ACCESS     = 0xf003f;

   /** La constante KEY_READ. */
   private static final int                    KEY_READ           = 0x20019;

   /** La constante KEY_WRITE. */
   private static final int                    KEY_WRITE          = 0x20006;

   /** user root. */
   private static Preferences                  userRoot           = Preferences.userRoot();

   /** system root. */
   private static Preferences                  systemRoot         = Preferences.systemRoot();

   /** user class. */
   private static Class<? extends Preferences> userClass          = userRoot.getClass();

   /** reg open key. */
   private static Method                       regOpenKey         = null;

   /** reg close key. */
   private static Method                       regCloseKey        = null;

   /** reg query value ex. */
   private static Method                       regQueryValueEx    = null;

   /** reg enum value. */
   private static Method                       regEnumValue       = null;

   /** reg query info key. */
   private static Method                       regQueryInfoKey    = null;

   /** reg enum key ex. */
   private static Method                       regEnumKeyEx       = null;

   /** reg create key ex. */
   private static Method                       regCreateKeyEx     = null;

   /** reg set value ex. */
   private static Method                       regSetValueEx      = null;

   /** reg delete key. */
   private static Method                       regDeleteKey       = null;

   /** reg delete value. */
   private static Method                       regDeleteValue     = null;

   static {
      try {
         regOpenKey = userClass.getDeclaredMethod("WindowsRegOpenKey", long.class, byte[].class, int.class);
         regOpenKey.setAccessible(true);
         regCloseKey = userClass.getDeclaredMethod("WindowsRegCloseKey", long.class);
         regCloseKey.setAccessible(true);
         regQueryValueEx = userClass.getDeclaredMethod("WindowsRegQueryValueEx", long.class, byte[].class);
         regQueryValueEx.setAccessible(true);
         regEnumValue = userClass.getDeclaredMethod("WindowsRegEnumValue", long.class, int.class, int.class);
         regEnumValue.setAccessible(true);
         regQueryInfoKey = userClass.getDeclaredMethod("WindowsRegQueryInfoKey1", long.class);
         regQueryInfoKey.setAccessible(true);
         regEnumKeyEx = userClass.getDeclaredMethod("WindowsRegEnumKeyEx", long.class, int.class, int.class);
         regEnumKeyEx.setAccessible(true);
         regCreateKeyEx = userClass.getDeclaredMethod("WindowsRegCreateKeyEx", long.class, byte[].class);
         regCreateKeyEx.setAccessible(true);
         regSetValueEx = userClass.getDeclaredMethod("WindowsRegSetValueEx", long.class, byte[].class, byte[].class);
         regSetValueEx.setAccessible(true);
         regDeleteValue = userClass.getDeclaredMethod("WindowsRegDeleteValue", long.class, byte[].class);
         regDeleteValue.setAccessible(true);
         regDeleteKey = userClass.getDeclaredMethod("WindowsRegDeleteKey", long.class, byte[].class);
         regDeleteKey.setAccessible(true);
      }
      catch (Exception e) {
         log.error(e.getLocalizedMessage());
      }
   }

   /**
    * Instancia un nuevo objeto de la clase windows registry.
    */
   private WindowsRegistry() {
   }

   /**
    * Read a value from key and value name.
    *
    * @param hkey
    *           HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
    * @param key
    *           valor para: key
    * @param valueName
    *           valor para: value name
    * @return the value
    * @throws IllegalArgumentException
    *            la illegal argument exception
    * @throws IllegalAccessException
    *            la illegal access exception
    * @throws InvocationTargetException
    *            la invocation target exception
    */
   public static String readString(long hkey, String key, String valueName)
         throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      if (hkey == HKEY_LOCAL_MACHINE) {
         return readString(systemRoot, hkey, key, valueName);
      }
      else if (hkey == HKEY_CURRENT_USER) {
         return readString(userRoot, hkey, key, valueName);
      }
      else {
         throw new IllegalArgumentException("hkey=" + hkey);
      }
   }

   /**
    * Read value(s) and value name(s) form given key.
    *
    * @param hkey
    *           HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
    * @param key
    *           valor para: key
    * @return the value name(s) plus the value(s)
    * @throws IllegalArgumentException
    *            la illegal argument exception
    * @throws IllegalAccessException
    *            la illegal access exception
    * @throws InvocationTargetException
    *            la invocation target exception
    */
   public static Map<String, String> readStringValues(long hkey, String key)
         throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      if (hkey == HKEY_LOCAL_MACHINE) {
         return readStringValues(systemRoot, hkey, key);
      }
      else if (hkey == HKEY_CURRENT_USER) {
         return readStringValues(userRoot, hkey, key);
      }
      else {
         throw new IllegalArgumentException("hkey=" + hkey);
      }
   }

   /**
    * Read the value name(s) from a given key.
    *
    * @param hkey
    *           HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
    * @param key
    *           valor para: key
    * @return the value name(s)
    * @throws IllegalArgumentException
    *            la illegal argument exception
    * @throws IllegalAccessException
    *            la illegal access exception
    * @throws InvocationTargetException
    *            la invocation target exception
    */
   public static List<String> readStringSubKeys(long hkey, String key)
         throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      if (hkey == HKEY_LOCAL_MACHINE) {
         return readStringSubKeys(systemRoot, hkey, key);
      }
      else if (hkey == HKEY_CURRENT_USER) {
         return readStringSubKeys(userRoot, hkey, key);
      }
      else {
         throw new IllegalArgumentException("hkey=" + hkey);
      }
   }

   /**
    * Create a key.
    *
    * @param hkey
    *           HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
    * @param key
    *           valor para: key
    * @throws IllegalArgumentException
    *            la illegal argument exception
    * @throws IllegalAccessException
    *            la illegal access exception
    * @throws InvocationTargetException
    *            la invocation target exception
    */
   public static void createKey(long hkey, String key) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      long[] ret;
      if (hkey == HKEY_LOCAL_MACHINE) {
         ret = createKey(systemRoot, hkey, key);
         regCloseKey.invoke(systemRoot, Long.valueOf(ret[0]));
      }
      else if (hkey == HKEY_CURRENT_USER) {
         ret = createKey(userRoot, hkey, key);
         regCloseKey.invoke(userRoot, Long.valueOf(ret[0]));
      }
      else {
         throw new IllegalArgumentException("hkey=" + hkey);
      }
      if (ret[1] != REG_SUCCESS) {
         throw new IllegalArgumentException("rc=" + ret[1] + "  key=" + key);
      }
   }

   /**
    * Write a value in a given key/value name.
    *
    * @param hkey
    *           valor para: hkey
    * @param key
    *           valor para: key
    * @param valueName
    *           valor para: value name
    * @param value
    *           valor para: value
    * @throws IllegalArgumentException
    *            la illegal argument exception
    * @throws IllegalAccessException
    *            la illegal access exception
    * @throws InvocationTargetException
    *            la invocation target exception
    */
   public static void writeStringValue(long hkey, String key, String valueName, String value)
         throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      if (hkey == HKEY_LOCAL_MACHINE) {
         writeStringValue(systemRoot, hkey, key, valueName, value);
      }
      else if (hkey == HKEY_CURRENT_USER) {
         writeStringValue(userRoot, hkey, key, valueName, value);
      }
      else {
         throw new IllegalArgumentException("hkey=" + hkey);
      }
   }

   /**
    * Delete a given key.
    *
    * @param hkey
    *           valor para: hkey
    * @param key
    *           valor para: key
    * @throws IllegalArgumentException
    *            la illegal argument exception
    * @throws IllegalAccessException
    *            la illegal access exception
    * @throws InvocationTargetException
    *            la invocation target exception
    */
   public static void deleteKey(long hkey, String key) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      int rc = -1;
      if (hkey == HKEY_LOCAL_MACHINE) {
         rc = deleteKey(systemRoot, hkey, key);
      }
      else if (hkey == HKEY_CURRENT_USER) {
         rc = deleteKey(userRoot, hkey, key);
      }
      if (rc != REG_SUCCESS) {
         throw new IllegalArgumentException("rc=" + rc + "  key=" + key);
      }
   }

   /**
    * delete a value from a given key/value name.
    *
    * @param hkey
    *           valor para: hkey
    * @param key
    *           valor para: key
    * @param value
    *           valor para: value
    * @throws IllegalArgumentException
    *            la illegal argument exception
    * @throws IllegalAccessException
    *            la illegal access exception
    * @throws InvocationTargetException
    *            la invocation target exception
    */
   public static void deleteValue(long hkey, String key, String value)
         throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      long rc = -1;
      if (hkey == HKEY_LOCAL_MACHINE) {
         rc = deleteValue(systemRoot, hkey, key, value);
      }
      else if (hkey == HKEY_CURRENT_USER) {
         rc = deleteValue(userRoot, hkey, key, value);
      }
      if (rc != REG_SUCCESS) {
         throw new IllegalArgumentException("rc=" + rc + "  key=" + key + "  value=" + value);
      }
   }

   // =====================

   /**
    * Delete value.
    *
    * @param root
    *           valor para: root
    * @param hkey
    *           valor para: hkey
    * @param key
    *           valor para: key
    * @param value
    *           valor para: value
    * @return long
    * @throws IllegalArgumentException
    *            la illegal argument exception
    * @throws IllegalAccessException
    *            la illegal access exception
    * @throws InvocationTargetException
    *            la invocation target exception
    */
   private static Long deleteValue(Preferences root, long hkey, String key, String value)
         throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      long[] handles = (long[]) regOpenKey.invoke(root, Long.valueOf(hkey), toCstr(key), Integer.valueOf(KEY_ALL_ACCESS));
      if (handles[1] != REG_SUCCESS) {
         return handles[1]; // can be REG_NOTFOUND, REG_ACCESSDENIED
      }
      long rc = ((Long) regDeleteValue.invoke(root, Long.valueOf(handles[0]), toCstr(value)));
      regCloseKey.invoke(root, Long.valueOf(handles[0]));
      return rc;
   }

   /**
    * Delete key.
    *
    * @param root
    *           valor para: root
    * @param hkey
    *           valor para: hkey
    * @param key
    *           valor para: key
    * @return int
    * @throws IllegalArgumentException
    *            la illegal argument exception
    * @throws IllegalAccessException
    *            la illegal access exception
    * @throws InvocationTargetException
    *            la invocation target exception
    */
   private static int deleteKey(Preferences root, long hkey, String key)
         throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      return ((Integer) regDeleteKey.invoke(root, Long.valueOf(hkey), toCstr(key))); // can REG_NOTFOUND, REG_ACCESSDENIED, REG_SUCCESS
   }

   /**
    * Read string.
    *
    * @param root
    *           valor para: root
    * @param hkey
    *           valor para: hkey
    * @param key
    *           valor para: key
    * @param value
    *           valor para: value
    * @return string
    * @throws IllegalArgumentException
    *            la illegal argument exception
    * @throws IllegalAccessException
    *            la illegal access exception
    * @throws InvocationTargetException
    *            la invocation target exception
    */
   private static String readString(Preferences root, long hkey, String key, String value)
         throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      long[] handles = (long[]) regOpenKey.invoke(root, Long.valueOf(hkey), toCstr(key), Integer.valueOf(KEY_READ));
      if (handles[1] != REG_SUCCESS) {
         return null;
      }
      byte[] valb = (byte[]) regQueryValueEx.invoke(root, Long.valueOf(handles[0]), toCstr(value));
      regCloseKey.invoke(root, Long.valueOf(handles[0]));
      return (valb != null ? new String(valb).trim() : null);
   }

   /**
    * Read string values.
    *
    * @param root
    *           valor para: root
    * @param hkey
    *           valor para: hkey
    * @param key
    *           valor para: key
    * @return map
    * @throws IllegalArgumentException
    *            la illegal argument exception
    * @throws IllegalAccessException
    *            la illegal access exception
    * @throws InvocationTargetException
    *            la invocation target exception
    */
   private static Map<String, String> readStringValues(Preferences root, long hkey, String key)
         throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      HashMap<String, String> results = new HashMap<>();
      long[] handles = (long[]) regOpenKey.invoke(root, Long.valueOf(hkey), toCstr(key), Integer.valueOf(KEY_READ));
      if (handles[1] != REG_SUCCESS) {
         return null;
      }
      long[] info = (long[]) regQueryInfoKey.invoke(root, Long.valueOf(handles[0]));

      int count = Integer.parseInt(String.valueOf(info[0])); // count
      int maxlen = Integer.parseInt(String.valueOf(info[3])); // value length max
      for (int index = 0; index < count; index++) {
         byte[] name = (byte[]) regEnumValue.invoke(root, Long.valueOf(handles[0]), Integer.valueOf(index), Integer.valueOf(maxlen + 1));
         String value = readString(hkey, key, new String(name));
         results.put(new String(name).trim(), value);
      }
      regCloseKey.invoke(root, Long.valueOf(handles[0]));
      return results;
   }

   /**
    * Read string sub keys.
    *
    * @param root
    *           valor para: root
    * @param hkey
    *           valor para: hkey
    * @param key
    *           valor para: key
    * @return list
    * @throws IllegalArgumentException
    *            la illegal argument exception
    * @throws IllegalAccessException
    *            la illegal access exception
    * @throws InvocationTargetException
    *            la invocation target exception
    */
   private static List<String> readStringSubKeys(Preferences root, long hkey, String key)
         throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      List<String> results = new ArrayList<>();
      long[] handles = (long[]) regOpenKey.invoke(root, Long.valueOf(hkey), toCstr(key), Integer.valueOf(KEY_READ));
      if (handles[1] != REG_SUCCESS) {
         return null;
      }
      long[] info = (long[]) regQueryInfoKey.invoke(root, Long.valueOf(handles[0]));

      int count = Integer.parseInt(String.valueOf(info[0])); // Fix: info[2] was being used here with wrong results. Suggested by davenpcj,
                                                             // confirmed by Petrucio
      int maxlen = Integer.parseInt(String.valueOf(info[3])); // value length max
      for (int index = 0; index < count; index++) {
         byte[] name = (byte[]) regEnumKeyEx.invoke(root, Long.valueOf(handles[0]), Integer.valueOf(index), Integer.valueOf(maxlen + 1));
         results.add(new String(name).trim());
      }
      regCloseKey.invoke(root, Long.valueOf(handles[0]));
      return results;
   }

   /**
    * Creates the key.
    *
    * @param root
    *           valor para: root
    * @param hkey
    *           valor para: hkey
    * @param key
    *           valor para: key
    * @return long[]
    * @throws IllegalArgumentException
    *            la illegal argument exception
    * @throws IllegalAccessException
    *            la illegal access exception
    * @throws InvocationTargetException
    *            la invocation target exception
    */
   private static long[] createKey(Preferences root, long hkey, String key)
         throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      return (long[]) regCreateKeyEx.invoke(root, Long.valueOf(hkey), toCstr(key));
   }

   /**
    * Write string value.
    *
    * @param root
    *           valor para: root
    * @param hkey
    *           valor para: hkey
    * @param key
    *           valor para: key
    * @param valueName
    *           valor para: value name
    * @param value
    *           valor para: value
    * @throws IllegalArgumentException
    *            la illegal argument exception
    * @throws IllegalAccessException
    *            la illegal access exception
    * @throws InvocationTargetException
    *            la invocation target exception
    */
   private static void writeStringValue(Preferences root, long hkey, String key, String valueName, String value)
         throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
      long[] handles = (long[]) regOpenKey.invoke(root, Long.valueOf(hkey), toCstr(key), Integer.valueOf(KEY_WRITE));
      if (handles[1] != REG_SUCCESS) {
         throw new IllegalArgumentException("rc=" + handles[1] + "  key=" + key);
      }
      regSetValueEx.invoke(root, Long.valueOf(handles[0]), toCstr(valueName), toCstr(value));
      if (handles[1] != REG_SUCCESS) {
         throw new IllegalArgumentException("rc=" + handles[1] + "  key=" + key);
      }
      regCloseKey.invoke(root, Long.valueOf(handles[0]));
   }

   /**
    * To cstr.
    *
    * @param str
    *           valor para: str
    * @return byte[]
    */
   // utility
   private static byte[] toCstr(String str) {
      byte[] result = new byte[str.length() + 1];

      for (int i = 0; i < str.length(); i++) {
         result[i] = (byte) str.charAt(i);
      }
      result[str.length()] = 0;
      return result;
   }
}