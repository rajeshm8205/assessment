package com.aem.assessment.core.models;

import com.adobe.cq.wcm.core.components.commons.link.LinkManager;
import com.adobe.cq.wcm.core.components.internal.models.v1.PageListItemImpl;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.components.Component;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;


public class MegamenuItemImpl extends PageListItemImpl implements MegamenuItem {
    protected List<MegamenuItem> children = Collections.emptyList();
    protected int level;
    protected boolean active;
    private boolean current;

    public MegamenuItemImpl(Page page, boolean active, boolean current, @NotNull LinkManager linkManager, int level, List<MegamenuItem> children, String parentId, Component component) {
        super(linkManager, page, parentId, component);
        this.active = active;
        this.current = current;
        this.level = level;
        this.children = children;
    }

    /**
     * @deprecated
     */
    @JsonIgnore
    @Deprecated
    public Page getPage() {
        return this.page;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isCurrent() {
        return this.current;
    }

    public List<MegamenuItem> getChildren() {
        return this.children;
    }

    public int getLevel() {
        return this.level;
    }
}
