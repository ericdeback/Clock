package App;
/*
 * Apache 2.0 license
 *
 * Subject to the terms and conditions of this License, each Contributor hereby
 * grants to You a perpetual, worldwide, non-exclusive, no-charge, royalty-free,
 * irrevocable copyright license to reproduce, prepare Derivative Works of,
 * publicly display, publicly perform, sublicense, and distribute the Work and such
 * Derivative Works in Source or Object form.
 */

import Listeners.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.time.*;
import java.util.*;

public class Clock extends Canvas implements Runnable {
    Frame frame = new Frame();
    public int width = 400;
    public int height = 400;
    public PopupMenu pum;
    //JFrame testFrame;

    public Clock() {

        this.setSize(new Dimension(width, height));
        this.setBackground(Color.BLACK);

        this.setSize(width, height);

        frame.add(this);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(true);
        frame.setAlwaysOnTop(false);

        width = this.getWidth();
        height = this.getHeight();

        pum = new PopupMenu();
        MenuItem onTop = new MenuItem("Set always on top");

        onTop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (frame.isAlwaysOnTop()) {
                    frame.setAlwaysOnTop(false);
                    onTop.setLabel("Set always on top");
                } else {
                    frame.setAlwaysOnTop(true);
                    onTop.setLabel("Unset always on top");
                }
            }
        });
        pum.add(onTop);
        this.add(pum);

        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });

        this.addMouseListener(new myMouseListener(this));
        Thread runner = new Thread(this);
        runner.start();
    }

    public void run() {

        // initialise world parameters

        // x = 0
        // y = 0
        // z = 0


        while (true) {

            // dynamics go here
            Date date = new Date();
            SimpleDateFormat fmt = new SimpleDateFormat("dd MMM YYYY");

            frame.setTitle(String.valueOf(fmt.format(new Date())));

            repaint();

            // t = t+dt;

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }
    }

    public void paint(Graphics g) {

        LocalDateTime now = LocalDateTime.now();
        int height = this.getHeight();
        int width = this.getWidth();
        int clockD = width<=height ? (int)(0.85*width) : (int)(0.85*height);
        int sechand = (int)(.9 * clockD/2);
        int minhand = (int)(.7 * clockD/2);
        int hourhand = (int)(.4 * clockD/2);
        double day = (double)now.getDayOfMonth();
        double hour = (double)now.getHour();
        double hour_12 = 0d;
        double minute = (double)now.getMinute();
        double second = (double)now.getSecond();
        double nano = (double)now.getNano();
        double mili = (double)System.currentTimeMillis();
        Random r = new Random();

        double x1,y1,x2,y2;
        int length=0;
        int strokewidth=1;
        x1=y1=x2=y2=0d;

        // deal with 24-hour clock
        hour_12 = hour;
        if(hour>12) hour_12 = hour-12d;

        Graphics2D g2 = (Graphics2D)g;

        //
        // draw clock shape
        //
        g.setColor(Color.WHITE);
        g.drawLine(0,0,1,1);

        // clock face
        g2.setStroke(new BasicStroke(1));
        g.setColor(Color.GRAY);
        g.fillOval((int)(width/2-clockD/2)-2, (int)(height/2-clockD/2)-2, clockD+1, clockD+1);

        // minute interval markers
        for (double phi=0; phi<60d; phi++) {
            length = 3;
            strokewidth = 1;
            if(phi%5==0) {
                length = 10;
                strokewidth=2;
            }

            x1=width / 2 + clockD / 2 * Math.sin(6.28d * phi / 60d);
            y1=height / 2 - clockD / 2 * Math.cos(6.28d * phi / 60d);
            x2=width / 2 + (clockD / 2 - length) * Math.sin(6.28d * phi / 60d);
            y2=height / 2 - (clockD / 2 - length) * Math.cos(6.28d * phi / 60d);

            if ((phi != second)) {
                g.setColor(Color.WHITE);
            } else {
                g.setColor(Color.RED);
            }

            g2.setStroke(new BasicStroke(strokewidth));
            g.drawLine((int)x1,(int)y1,(int)x2,(int)y2);
        }

        // draw time on the current second position
        g.setColor(Color.WHITE);
        int fontSize = width/25;
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
        DecimalFormat df = new DecimalFormat("00");

        // hour
        int hourStringWidth = g.getFontMetrics().stringWidth(df.format(hour)+":");
        double phi = Math.asin(2d*(double)hourStringWidth/clockD); // angle for h, m & s calcs

        double theta = Math.toRadians((second) * 360d / 60d) -phi; // offset for h
        g2.rotate(theta, (int)(width/2)-2, (int)(height/2)-2);
        g.drawString(df.format(hour)+":", (int)(width/2-hourStringWidth/2), (int)((height-clockD)/2)-7);
        g2.rotate(-theta, (int)(width/2)-2, (int)(height/2)-2); //reset rotate angle

        // minute
        int minuteStringWidth = g.getFontMetrics().stringWidth(df.format(minute)+":");
        g2.rotate(Math.toRadians(second*360d/60d), (int)(width/2)-2, (int)(height/2)-2);
        g.drawString(df.format(minute)+":", (int)(width/2-minuteStringWidth/2), (int)((height-clockD)/2)-7);
        g2.rotate(-Math.toRadians(second*360d/60d), (int)(width/2)-2, (int)(height/2)-2);

        // second
        int secondStringWidth = g.getFontMetrics().stringWidth(df.format(second));
        theta = Math.toRadians(second * 360d / 60d)+phi; // offset for s

        g2.rotate(theta, (int)(width/2)-2, (int)(height/2)-2);
        g.drawString(df.format(second), (int)(width/2-secondStringWidth/2), (int)((height-clockD)/2)-7);
        g2.rotate(-theta, (int)(width/2)-2, (int)(height/2)-2); //reset rotate angle

        // second hand
        Random rnd = new Random();
        g2.setStroke(new BasicStroke(2));
        //g.setColor(new Color(rnd.nextInt(255), rnd.nextInt(255),rnd.nextInt(255)));
        g.setColor(Color.RED);

        g.drawLine((int)(width/2), (int)(height/2), width/2 + (int)(sechand*Math.sin(6.28d*second/60d)), height/2-(int)(sechand * Math.cos(6.28d*second/60d)));

        // minute hand
        g2.setStroke(new BasicStroke(3));
        g.setColor(Color.RED);
        g.drawLine((int)(width/2), (int)(height/2), width/2 + (int)(minhand*Math.sin(6.28d*minute/60d)), height/2-(int)(minhand * Math.cos(6.28d*minute/60d)));

        // hour
        g2.setStroke(new BasicStroke(5));
        g.setColor(Color.RED);
        g.drawLine(
                (int)(width/2),
                (int)(height/2),
                width/2 + (int)(hourhand * Math.sin(6.28d*hour_12/12d + 6.28d/12d*minute/60d)),
                height/2- (int)(hourhand * Math.cos(6.28d*hour_12/12d + 6.28d/12d*minute/60d))
        );

        // centre dot
        g.setColor(Color.DARK_GRAY);
        g.fillOval((int)(width/2)-3, (int)(height/2)-3,6,6);


    }

    public void update(Graphics g) {
        Graphics offgc;
        Image offscreen = null;

        // create the offscreen buffer and associated Graphics
        offscreen = createImage(frame.getWidth(), frame.getHeight());
        offgc = offscreen.getGraphics();
        // clear the exposed area
        offgc.setColor(getBackground());
        offgc.fillRect(0, 0, frame.getWidth(), frame.getHeight());
        offgc.setColor(getForeground());
        // do normal redraw
        paint(offgc);
        // transfer offscreen to window
        g.drawImage(offscreen, 0, 0, this);
    }

    void setmySize(Dimension d) {
        this.setSize(d);
    }

    public static void main(String[] args) {
        new Clock();

        //double a = 1d;
        //DecimalFormat df = new DecimalFormat("00");
        //System.out.println("Number formatted using DecimalFormat" + df.format(09.0));
    }

}