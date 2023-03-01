package com.zedo.fxcomponent.components;

import com.zedo.fxcomponent.utils.FXUtil;
import javafx.collections.ObservableList;
import javafx.css.Selector;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Region;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Container extends Region {

    public Set<Node> findNodes(String selector) {
        Selector s = Selector.createSelector(selector);
        HashSet<Node> nodes = new HashSet<>();
        FXUtil.loopNode(this, node -> {
            if (s.applies(node))
                nodes.add(node);
            return true;
        });
        return nodes;
    }

    public Node findNode(String selector) {
        Selector s = Selector.createSelector(selector);
        AtomicReference<Node> findNode = new AtomicReference<>();
        FXUtil.loopNode(this, node -> {
            if (s.applies(node)) {
                findNode.set(node);
                return false;
            }
            return true;
        });
        return findNode.get();
    }

    @Override
    protected void layoutChildren() {
        List<Node> nodes = this.getManagedChildren();
        for (Node node : nodes) {
            Insets inset = getPadding();

            node.setLayoutX(inset.getLeft());
            node.setLayoutY(inset.getTop());
            node.resize(getWidth() - inset.getLeft() - inset.getRight(), getHeight() - inset.getTop() - inset.getBottom());
        }
    }

    public ObservableList<Node> getChildren() {
        return super.getChildren();
    }

    public Container(Node... var1) {
        this.getChildren().addAll(var1);
    }
}
