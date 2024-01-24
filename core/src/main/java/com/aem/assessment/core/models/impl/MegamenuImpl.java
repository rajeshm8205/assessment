package com.aem.assessment.core.models.impl;

import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.wcm.core.components.commons.link.LinkManager;
import com.adobe.cq.wcm.core.components.internal.LocalizationUtils;
import com.adobe.cq.wcm.core.components.internal.Utils;
import com.adobe.cq.wcm.core.components.util.AbstractComponentImpl;
import com.aem.assessment.core.models.Megamenu;
import com.aem.assessment.core.models.MegamenuItem;
import com.aem.assessment.core.models.MegamenuItemImpl;
import com.day.cq.wcm.api.LanguageManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.designer.Style;
import com.day.cq.wcm.msm.api.LiveRelationshipManager;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//        resourceType = {"core/wcm/components/navigation/v1/navigation"}
@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {Megamenu.class, ComponentExporter.class},
        resourceType = {MegamenuImpl.RESOURCE_TYPE}


)
@Exporter(
        name = "jackson",
        extensions = {"json"}
)
public class MegamenuImpl extends AbstractComponentImpl implements Megamenu {
    //    public static final String RESOURCE_TYPE = "core/wcm/components/navigation/v1/navigation";
    public static final String RESOURCE_TYPE = "assessment/components/megamenu";
    private static final String PN_ACCESSIBILITY_LABEL = "accessibilityLabel";
    @Self
    private SlingHttpServletRequest request;
    @Self
    private LinkManager linkManager;
    @ScriptVariable
    private Page currentPage;
    @ScriptVariable
    private Style currentStyle;
    @OSGiService
    private LanguageManager languageManager;
    @OSGiService
    private LiveRelationshipManager relationshipManager;
    private @Nullable String accessibilityLabel;
    private int structureStart;
    private int structureDepth;
    private Page navigationRootPage;
    private List<MegamenuItem> items;

    public MegamenuImpl() {
    }

    @PostConstruct
    private void initModel() {
        ValueMap properties = this.resource.getValueMap();
        this.structureDepth = (Integer) properties.get("structureDepth", this.currentStyle.get("structureDepth", -1));
        boolean collectAllPages = (Boolean) properties.get("collectAllPages", this.currentStyle.get("collectAllPages", true));
        if (collectAllPages) {
            this.structureDepth = -1;
        }

        if (!this.currentStyle.containsKey("structureStart") && !properties.containsKey("structureStart")) {
            boolean skipNavigationRoot = (Boolean) properties.get("skipNavigationRoot", this.currentStyle.get("skipNavigationRoot", true));
            if (skipNavigationRoot) {
                this.structureStart = 1;
            } else {
                this.structureStart = 0;
            }
        } else {
            this.structureStart = (Integer) properties.get("structureStart", this.currentStyle.get("structureStart", 1));
        }

    }

    private Page getNavigationRoot() {
        if (this.navigationRootPage == null) {
            String navigationRootPath = (String) Optional.ofNullable(this.resource.getValueMap().get("navigationRoot", String.class)).orElseGet(() -> {
                return (String) this.currentStyle.get("navigationRoot", String.class);
            });
            this.navigationRootPage = (Page) LocalizationUtils.getLocalPage(navigationRootPath, this.currentPage, this.request.getResourceResolver(), this.languageManager, this.relationshipManager).orElseGet(() -> {
                return this.currentPage.getPageManager().getPage(navigationRootPath);
            });
        }

        return this.navigationRootPage;
    }

    public List<MegamenuItem> getItems() {
        if (this.items == null) {
            this.items = (List) ((Stream) Optional.ofNullable(this.getNavigationRoot()).map((navigationRoot) -> {
                return this.getRootItems(navigationRoot, this.structureStart);
            }).orElseGet(Stream::empty)).map((item) -> {
                return this.createNavigationItem(item, this.getItems(item));
            }).collect(Collectors.toList());
        }

        return Collections.unmodifiableList(this.items);

        return null;
    }

    protected MegamenuItem newNavigationItem(Page page, boolean active, boolean current, @NotNull LinkManager linkManager, int level, List<MegamenuItem> children, String parentId, Component component) {
        return new MegamenuItemImpl(page, active, current, linkManager, level, children, parentId, component);
    }

    public @Nullable String getAccessibilityLabel() {
        if (this.accessibilityLabel == null) {
            this.accessibilityLabel = (String) this.resource.getValueMap().get("accessibilityLabel", String.class);
        }

        return this.accessibilityLabel;
    }

    public @NotNull String getExportedType() {
        return this.resource.getResourceType();
    }

    private List<MegamenuItem> getItems(@NotNull Page subtreeRoot) {
        if (this.structureDepth >= 0 && subtreeRoot.getDepth() - this.getNavigationRoot().getDepth() >= this.structureDepth) {
            return Collections.emptyList();
        } else {
            Iterator<Page> childIterator = subtreeRoot.listChildren(new PageFilter());
            return (List) StreamSupport.stream((() -> {
                return childIterator;
            }).spliterator(), false).map((item) -> {
                return this.createNavigationItem(item, this.getItems(item));
            }).collect(Collectors.toList());
        }
        return null;
    }

    private Stream<Page> getRootItems(@NotNull Page navigationRoot, int structureStart) {
        if (structureStart < 1) {
            return Stream.of(navigationRoot);
        } else {
            Iterator<Page> childIterator = navigationRoot.listChildren(new PageFilter());
            return StreamSupport.stream((() -> {
                return childIterator;
            }).spliterator(), false).flatMap((child) -> {
                return this.getRootItems(child, structureStart - 1);
            });
        }
        return null;
    }

    private MegamenuItem createNavigationItem(@NotNull Page page, @NotNull List<MegamenuItem> children) {
        int level = page.getDepth() - (this.getNavigationRoot().getDepth() + this.structureStart);
        boolean current = this.checkCurrent(page);
        boolean selected = this.checkSelected(page, current);
        return this.newNavigationItem(page, selected, current, this.linkManager, level, children, this.getId(), this.component);
    }

    private boolean checkSelected(@NotNull Page page, boolean current) {
        return current || this.currentPage.getPath().startsWith(page.getPath() + "/");
    }

    private boolean checkCurrent(@NotNull Page page) {
        return this.currentPage.equals(page) || this.currentPageIsRedirectTarget(page);
    }

    private boolean currentPageIsRedirectTarget(@NotNull Page page) {
        return this.currentPage.equals(Utils.resolveRedirects(page).getLeft());
    }
}
