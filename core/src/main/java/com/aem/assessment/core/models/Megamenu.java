package com.aem.assessment.core.models;

import com.adobe.cq.wcm.core.components.models.Component;

import java.util.List;

public interface Megamenu extends Component {
    String PN_NAVIGATION_ROOT = "navigationRoot";
    /**
     * @deprecated
     */
    @Deprecated
    String PN_SKIP_NAVIGATION_ROOT = "skipNavigationRoot";
    String PN_STRUCTURE_START = "structureStart";
    String PN_COLLECT_ALL_PAGES = "collectAllPages";
    String PN_STRUCTURE_DEPTH = "structureDepth";

    default List<MegamenuItem> getItems() {
        return null;
    }

    default String getAccessibilityLabel() {
        return null;
    }
}
