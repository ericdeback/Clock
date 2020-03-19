package Listeners;

import App.*;

import java.awt.event.*;

public class myMouseListener implements java.awt.event.MouseListener {
    Clock clock;

    public myMouseListener(Clock clock) {
        this.clock=clock;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if (e.getButton()==3 || e.isControlDown()) { //right-click or ctrl-right-click
            clock.pum.show(clock, e.getX(),e.getY());
        }

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
