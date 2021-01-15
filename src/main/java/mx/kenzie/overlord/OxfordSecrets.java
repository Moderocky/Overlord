package mx.kenzie.overlord;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

class OxfordSecrets {
    
    public static final Class<?> SHARED_SECRETS_CLASS;
    
    static Object javaAWTAccess;
    static Object javaAWTFontAccess;
    static Object javaBeansAccess;
    static Object javaLangAccess;
    static Object javaLangInvokeAccess;
    static Object javaLangModuleAccess;
    static Object javaLangRefAccess;
    static Object javaLangReflectAccess;
    static Object javaIOAccess;
    static Object javaIOFileDescriptorAccess;
    static Object javaIOFilePermissionAccess;
    static Object javaIORandomAccessFileAccess;
    static Object javaObjectInputStreamReadString;
    static Object javaObjectInputStreamAccess;
    static Object javaObjectInputFilterAccess;
    static Object javaNetInetAddressAccess;
    static Object javaNetHttpCookieAccess;
    static Object javaNetUriAccess;
    static Object javaNetURLAccess;
    static Object javaNioAccess;
    static Object javaUtilJarAccess;
    static Object javaUtilZipFileAccess;
    static Object javaUtilResourceBundleAccess;
    static Object javaSecurityAccess;
    static Object javaSecuritySignatureAccess;
    static Object javaxCryptoSealedObjectAccess;
    
    static {
        try {
            SHARED_SECRETS_CLASS = Class.forName("jdk.internal.access.SharedSecrets");
            Overlord.breakEncapsulation(OxfordSecrets.class, SHARED_SECRETS_CLASS, true);
            Overlord.allowAccess(OxfordSecrets.class, SHARED_SECRETS_CLASS, true);
            for (Field field : OxfordSecrets.class.getDeclaredFields()) {
                if (Modifier.isFinal(field.getModifiers())) continue;
                Field declared = SHARED_SECRETS_CLASS.getDeclaredField(field.getName());
                declared.setAccessible(true);
                field.set(null, declared.get(null));
            }
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static <T> T javaLangReflectAccess() {
        return (T) OxfordSecrets.javaLangReflectAccess;
    }
    
    public static <T> T javaLangAccess() {
        return (T) OxfordSecrets.javaLangAccess;
    }
    
    public static <T> T javaLangInvokeAccess() {
        return (T) OxfordSecrets.javaLangInvokeAccess;
    }
    
    public static <T> T javaLangRefAccess() {
        return (T) OxfordSecrets.javaLangRefAccess;
    }
    
    public static <T> T javaLangModuleAccess() {
        return (T) OxfordSecrets.javaLangModuleAccess;
    }
    
    public static <T> T javaAWTAccess() {
        return (T) OxfordSecrets.javaAWTAccess;
    }
    
    public static <T> T javaAWTFontAccess() {
        return (T) OxfordSecrets.javaAWTFontAccess;
    }
    
    public static <T> T javaBeansAccess() {
        return (T) OxfordSecrets.javaBeansAccess;
    }
    
    public static <T> T javaIOAccess() {
        return (T) OxfordSecrets.javaIOAccess;
    }
    
    public static <T> T javaNioAccess() {
        return (T) OxfordSecrets.javaNioAccess;
    }
    
    public static <T> T javaIOFileDescriptorAccess() {
        return (T) OxfordSecrets.javaIOFileDescriptorAccess;
    }
    
    public static <T> T javaIOFilePermissionAccess() {
        return (T) OxfordSecrets.javaIOFilePermissionAccess;
    }
    
    public static <T> T javaNetHttpCookieAccess() {
        return (T) OxfordSecrets.javaNetHttpCookieAccess;
    }
    
    public static <T> T javaNetURLAccess() {
        return (T) OxfordSecrets.javaNetURLAccess;
    }
    
    public static <T> T javaSecurityAccess() {
        return (T) OxfordSecrets.javaSecurityAccess;
    }
    
    public static <T> T javaIORandomAccessFileAccess() {
        return (T) OxfordSecrets.javaIORandomAccessFileAccess;
    }
    
    public static <T> T javaObjectInputStreamReadString() {
        return (T) OxfordSecrets.javaObjectInputStreamReadString;
    }
    
    public static <T> T javaObjectInputStreamAccess() {
        return (T) OxfordSecrets.javaObjectInputStreamAccess;
    }
    
    public static <T> T javaObjectInputFilterAccess() {
        return (T) OxfordSecrets.javaObjectInputFilterAccess;
    }
    
    public static <T> T javaNetInetAddressAccess() {
        return (T) OxfordSecrets.javaNetInetAddressAccess;
    }
    
    public static <T> T javaNetUriAccess() {
        return (T) OxfordSecrets.javaNetUriAccess;
    }
    
    public static <T> T javaUtilJarAccess() {
        return (T) OxfordSecrets.javaUtilJarAccess;
    }
    
    public static <T> T javaUtilZipFileAccess() {
        return (T) OxfordSecrets.javaUtilZipFileAccess;
    }
    
    public static <T> T javaUtilResourceBundleAccess() {
        return (T) OxfordSecrets.javaUtilResourceBundleAccess;
    }
    
    public static <T> T javaSecuritySignatureAccess() {
        return (T) OxfordSecrets.javaSecuritySignatureAccess;
    }
    
    public static <T> T javaxCryptoSealedObjectAccess() {
        return (T) OxfordSecrets.javaxCryptoSealedObjectAccess;
    }
    
}
