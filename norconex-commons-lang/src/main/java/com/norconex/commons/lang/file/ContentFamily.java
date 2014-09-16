package com.norconex.commons.lang.file;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Represents a family of content types.  Typically, a broader, conceptual
 * object for related content types.
 * <p/>
 * To provide your own content type mappings or display names, copy the 
 * appropriate <code>.properties</code> file to your classpath root, with
 * the word "custom" inserted: <code>ContentFamily-custom-[...]</code>.
 * The actual custom names and classpath location are:
 * <p/>
 * <table border="1">
 *   <tr>
 *     <th>Original</td>
 *     <th>Custom</td>
 *   </tr>
 *   <tr>
 *     <td>com.norconex.commmons.lang.file.ContentFamily-mappings.properties</td>
 *     <td>ContentFamily-custom-mappings.properties</td>
 *   </tr>
 *   <tr>
 *     <td>com.norconex.commmons.lang.file.ContentFamily-name[_locale].properties</td>
 *     <td>ContentFamily-custom-name[_locale].properties</td>
 *   </tr>
 * </table> 
 * @author Pascal Essiembre
 * @since 1.4.0
 */
public final class ContentFamily {
    
    private static final Logger LOG = LogManager.getLogger(ContentFamily.class);
    
    private static final ResourceBundle BUNDLE_MAPPINGS;
    static {
        ResourceBundle bundle = null;
        try {
            bundle = ResourceBundle.getBundle(
                    ContentFamily.class.getSimpleName() + "-custom-mappings");
        } catch (MissingResourceException e) {
            bundle = ResourceBundle.getBundle(
                    ContentFamily.class.getName() + "-mappings");
        }
        BUNDLE_MAPPINGS = bundle;
    }

    private static final Map<String, String> WILD_MAPPINGS = 
            new ListOrderedMap<>();
            
    private static final Map<String, ContentFamily> FAMILIES = new HashMap<>();
    static {
        for (String contentType : BUNDLE_MAPPINGS.keySet()) {
            String familyId = BUNDLE_MAPPINGS.getString(contentType);
            if (contentType.startsWith("DEFAULT")) {
                String partialContentType = 
                        contentType.replaceFirst("DEFAULT\\.{0,1}", "");
                WILD_MAPPINGS.put(partialContentType, familyId);
            }
        }
    }
    
    private static final Map<Locale, ResourceBundle> BUNDLE_DISPLAYNAMES =
            new HashMap<>();
    
    private final String id;
    
    private ContentFamily(String id) {
        this.id = id;
    }

    public static ContentFamily valueOf(String familyId) {
        ContentFamily family = FAMILIES.get(familyId);
        if (family == null) {
            family = new ContentFamily(familyId);
            FAMILIES.put(familyId, family);
        }
        return family;
    }
    
    public static ContentFamily forContentType(ContentType contentType) {
        if (contentType == null) {
            return null;
        }
        return forContentType(contentType.toString());
    }
    public static ContentFamily forContentType(String contentType) {
        if (StringUtils.isBlank(contentType)) {
            return null;
        }
        String familyId = null;
        if (BUNDLE_MAPPINGS.containsKey(contentType)) {
            familyId = BUNDLE_MAPPINGS.getString(contentType);
        }
        if (familyId == null) {
            for (String partialContentType : WILD_MAPPINGS.keySet()) {
                if (contentType.startsWith(partialContentType)) {
                    familyId = WILD_MAPPINGS.get(partialContentType);
                    break;
                }
            }
        }
        return valueOf(familyId);
    }
    
    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return getDisplayName(Locale.getDefault());
    }
    public String getDisplayName(Locale locale) {
        Locale safeLocale = locale;
        if (safeLocale == null) {
            safeLocale = Locale.getDefault();
        }
        try {
            return getDisplayBundle(safeLocale).getString(id);
        } catch (MissingResourceException e) {
            LOG.debug("Could not find display name for content family: " + id);
        }
        return "[" + id + "]";
    }
    private ResourceBundle getDisplayBundle(Locale locale) {
        ResourceBundle bundle = BUNDLE_DISPLAYNAMES.get(locale);
        if (bundle != null) {
            return bundle;
        }
        try {
            bundle = ResourceBundle.getBundle(
                    ContentFamily.class.getSimpleName()
                            + "-custom-names", locale);
        } catch (MissingResourceException e) {
            bundle = ResourceBundle.getBundle(
                    ContentFamily.class.getName() + "-names", locale);
        }
        BUNDLE_DISPLAYNAMES.put(locale, bundle);
        return bundle;
    }
    

    public boolean contains(ContentType contentType) {
        if (contentType == null) {
            return false;
        }
        return contains(contentType.toString());
    }
    public boolean contains(String contentType) {
        ContentFamily family = forContentType(contentType);
        return family == this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ContentFamily other = (ContentFamily) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getId();
    }
    
}
