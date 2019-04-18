/*
   Copyright (c) 2017 Ahome' Innovation Technologies. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.ait.lienzo.client.core.mediator;

import elemental2.dom.UIEvent;

import com.ait.lienzo.client.core.event.AbstractNodeHumanInputEvent;
import com.ait.lienzo.tools.client.event.INodeEvent;
import com.ait.lienzo.client.core.event.NodeMouseDownEvent;
import com.ait.lienzo.client.core.event.NodeMouseMoveEvent;
import com.ait.lienzo.client.core.event.NodeMouseUpEvent;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Transform;
import com.ait.lienzo.tools.client.event.INodeEvent.Type;
import com.gwtlienzo.event.shared.EventHandler;

/**
 * MousePanMediator provides pan behavior similar to dragging the mouse in Google Maps.
 * Only the zoomable Layers are affected.
 * 
 * @see Mediators
 * 
 * @since 1.1
 */
public class MousePanMediator extends AbstractMediator
{
    private Point2D   m_last             = new Point2D(0,0);

    private boolean   m_dragging         = false;

    private Transform m_inverseTransform = null;

    public MousePanMediator()
    {
    }

    public MousePanMediator(final IEventFilter... filters)
    {
        setEventFilter(EventFilter.and(filters));
    }

    @Override
    public void cancel()
    {
        m_dragging = false;
    }

    @Override
    public <H extends EventHandler> boolean handleEvent(Type<H> type, final UIEvent event, int x, int y)
    {
        if (type == NodeMouseMoveEvent.getType())
        {
            if (m_dragging)
            {
                onMouseMove(x, y);

                return true;
            }
            return false;
        }
        else if (type == NodeMouseDownEvent.getType())
        {
            final IEventFilter filter = getEventFilter();

            if ((null == filter) || (false == filter.isEnabled()) || (filter.test(event)))
            {
                onMouseDown(x, y);

                return true;
            }
            return false;
        }
        else if (type == NodeMouseUpEvent.getType())
        {
            if (m_dragging)
            {
                onMouseUp();

                return true;
            }
        }
        return false;
    }

    protected void onMouseDown(int x, int y)
    {
        m_last = new Point2D(x, y);

        m_dragging = true;

        Transform transform = getTransform();

        if (transform == null)
        {
            setTransform(transform = new Transform());
        }
        m_inverseTransform = transform.getInverse();

        m_inverseTransform.transform(m_last, m_last);
    }

    protected void onMouseMove(int x, int y)
    {
        final Point2D curr = new Point2D(x, y);

        m_inverseTransform.transform(curr, curr);

        setTransform(getTransform().copy().translate(curr.getX() - m_last.getX(), curr.getY() - m_last.getY()));

        m_last = curr;

        if (isBatchDraw())
        {
            getViewport().getScene().batch();
        }
        else
        {
            getViewport().getScene().draw();
        }
    }

    protected void onMouseUp()
    {
        cancel();
    }
}
