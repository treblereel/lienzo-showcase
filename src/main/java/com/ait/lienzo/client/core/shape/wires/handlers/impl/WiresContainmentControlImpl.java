package com.ait.lienzo.client.core.shape.wires.handlers.impl;

import com.ait.lienzo.client.core.shape.wires.IContainmentAcceptor;
import com.ait.lienzo.client.core.shape.wires.WiresContainer;
import com.ait.lienzo.client.core.shape.wires.WiresLayer;
import com.ait.lienzo.client.core.shape.wires.WiresManager;
import com.ait.lienzo.client.core.shape.wires.WiresShape;
import com.ait.lienzo.client.core.shape.wires.handlers.WiresContainmentControl;
import com.ait.lienzo.client.core.shape.wires.picker.ColorMapBackedPicker;
import com.ait.lienzo.client.core.types.Point2D;

public class WiresContainmentControlImpl extends AbstractWiresParentPickerControl
        implements WiresContainmentControl {

    public WiresContainmentControlImpl(WiresShape shape,
                                       ColorMapBackedPicker.PickerOptions pickerOptions) {

        super(shape,
              pickerOptions);
    }

    public WiresContainmentControlImpl(WiresParentPickerControlImpl parentPickerControl) {
        super(parentPickerControl);
    }

    public static Point2D calculateCandidateLocation(final WiresParentPickerControlImpl parentPickerControl) {
        final WiresLayer m_layer = parentPickerControl.getShape().getWiresManager().getLayer();
        final WiresContainer parent = parentPickerControl.getParent();
        final Point2D current = parentPickerControl.getShapeLocation();
        if (parent == null || parent == m_layer) {
            return current;
        } else {
            final Point2D trgAbsOffset = parent.getComputedLocation();
            return current.sub(trgAbsOffset);
        }
    }

    @Override
    public WiresContainmentControl setEnabled(final boolean enabled) {
        if (enabled) {
            enable();
        } else {
            disable();
        }
        return this;
    }

    @Override
    public Point2D getAdjust() {
        return new Point2D(0,
                           0);
    }

    @Override
    public boolean isAllow() {
        return runAcceptor(true);
    }

    @Override
    public boolean accept() {
        return runAcceptor(false);
    }

    private boolean runAcceptor(final boolean allowNotAccept) {
        if (!isEnabled()) {
            return false;
        }
        final WiresShape shape = getShape();
        final WiresContainer parent = getParent();
        final WiresLayer m_layer = getWiresLayer();
        final WiresManager wiresManager = m_layer.getWiresManager();
        final IContainmentAcceptor containmentAcceptor = wiresManager.getContainmentAcceptor();
        final WiresShape[] shapes = {shape};
        final boolean isParentLayer = null == parent || parent instanceof WiresLayer;
        final WiresContainer candidateParent = isParentLayer ? m_layer : parent;
        final boolean isAllowed = containmentAcceptor.containmentAllowed(candidateParent,
                                                                         shapes);
        if (!allowNotAccept && isAllowed) {
            return containmentAcceptor.acceptContainment(candidateParent,
                                                         shapes);
        }
        return isAllowed;
    }

    @Override
    public Point2D getCandidateLocation() {
        return calculateCandidateLocation(getParentPickerControl());
    }

    @Override
    public void execute() {
        if (isEnabled()) {
            addIntoParent(getShape(),
                          getParent(),
                          getCandidateLocation());
        }
    }

    @Override
    public void clear() {
    }

    @Override
    public void reset() {
        if (isEnabled()) {
            if (!getParentPickerControl().getShapeLocationControl().isStartDocked() &&
                    getParentPickerControl().getInitialParent() != getShape().getParent()) {
                addIntoParent(getShape(),
                              getParentPickerControl().getInitialParent(),
                              getParentPickerControl().getShapeLocationControl().getShapeInitialLocation());
                getShape().setDockedTo(null);
            }
        }
    }

    @Override
    public void destroy() {
        clear();
    }

    private void addIntoParent(final WiresShape shape,
                               final WiresContainer parent,
                               final Point2D location) {
        final WiresLayer m_layer = getWiresLayer();
        if (parent == null || parent == m_layer) {
            m_layer.getLayoutHandler().add(shape,
                                           m_layer,
                                           location);
        } else {
            parent.getLayoutHandler().add(shape,
                                          parent,
                                          location);
        }
        shape.setDockedTo(null);
    }

    private WiresLayer getWiresLayer() {
        return getParentPickerControl().getWiresLayer();
    }
}
