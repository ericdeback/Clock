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
import org.apache.commons.io.*;
import org.json.*;

import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.text.*;
import java.time.*;
import java.util.Timer;
import java.util.*;

import static java.lang.Integer.*;

public class Clock extends Canvas implements Runnable {

    String version = "1.0.5";


    Frame frame = new Frame("");
    public int width = 400;
    public int height = 400;
    public PopupMenu popUpMenu;
    PopupMenu timeZoneMenu;

    Graphics2D g2;

    int clockD;
    ZonedDateTime now_zoned;

    double day;
    double hour;
    double hour_12;
    double minute;
    double second;
    Secondhand secondhand;
    Minutehand minutehand;
    Hourhand hourhand;
    String timeZoneStr = "Europe/London";
    String formatString = "dd MMM YYYY";
    MenuItem onTopMenuItem; // now an instance variable
    TimeZone currentTimeZone;
    String weatherURL = "http://api.openweathermap.org/data/2.5/weather?q=London,uk&APPID=b2a8bc257fa01634206954930e5a6301";
    Image weatherImage = null;

    void getWeatherUpdate() {
        System.out.println("Getting weather update");
        try {
            JSONObject json_weather = new JSONObject(IOUtils.toString(new URL(weatherURL), Charset.forName("UTF-8")));
            JSONArray weatherdetails = (JSONArray) json_weather.get("weather");
            JSONObject weatherobj = weatherdetails.getJSONObject(0);
            String weathericoncode = weatherobj.get("icon").toString();
            weatherImage = ImageIO.read(new URL("http://openweathermap.org/img/wn/"+weathericoncode+"@2x.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Clock() throws IOException {

        // then schedule it to run every 10 mins (as per openweathermap requests)
        Timer timer = new Timer("RefreshWeather");
        TimerTask task = new TimerTask(){

            @Override
            public void run() {
                getWeatherUpdate();
            }
        };

        long freq = 10*60*1000L; // 10 delay in ms
        timer.schedule(task, 0L, freq);

/*
        for(int i = 0 ; i < json_weather.length(); i++) {
            list.add(json_weather.getJSONObject().getString("coord"));
            System.out.println(json_weather.getJSONObject(i).getString("username")); // display usernames
        }
*/

        secondhand = new Secondhand(this);
        minutehand = new Minutehand(this);
        hourhand = new Hourhand(this);

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

        popUpMenu = new PopupMenu();
        onTopMenuItem = new MenuItem("Set always on top");
        timeZoneMenu = new PopupMenu("TimeZone");
        PopupMenu dateFormat = new PopupMenu("Date format");
        MenuItem aboutMenuItem = new MenuItem("About...");

        MenuItem ddMMYYYY = new MenuItem("dd MM YYYY");
        MenuItem HHmma = new MenuItem("HH mm a");
        MenuItem DDDddMMM = new MenuItem("DDD dd MMM");
        MenuItem HHmmssSSS = new MenuItem("HH mm ss SSS");

        ddMMYYYY.addActionListener(e-> {
            formatString = "dd MMM YYYY";
        });
        HHmma.addActionListener(e-> {
            formatString = "HH mm a";
        });
        DDDddMMM.addActionListener(e->{
            formatString = "E dd MMM";
        });
        HHmmssSSS.addActionListener(e->{
            formatString = "HH mm ss SSS";
        });

        dateFormat.add(ddMMYYYY);
        dateFormat.add(HHmma);
        dateFormat.add(DDDddMMM);
        dateFormat.add(HHmmssSSS);

        // Hard coded for now
        MenuItem London = new MenuItem("Europe/London");
        MenuItem Rome = new MenuItem("Europe/Rome");
        MenuItem EST = new MenuItem("EST");
        MenuItem Sydney = new MenuItem("Australia/Sydney");

        London.addActionListener(e-> {
            timeZoneStr = "Europe/London";
        });
        Rome.addActionListener(e-> {
            timeZoneStr = "Europe/Rome";
        });
        EST.addActionListener(e-> {
            timeZoneStr = "EST";
        });
        Sydney.addActionListener(e-> {
            timeZoneStr = "Australia/Sydney";
        });
        aboutMenuItem.addActionListener(e-> {
            JOptionPane.showMessageDialog(this, "Version " + version);
        });

        timeZoneMenu.add(EST);
        timeZoneMenu.add(London);
        timeZoneMenu.add(Rome);
        timeZoneMenu.add(Sydney);

        onTopMenuItem.addActionListener(e-> {
                if (frame.isAlwaysOnTop()) {
                    onTopMenuItem.setLabel("Set always on top");
                    frame.setAlwaysOnTop(false);
                } else {
                    onTopMenuItem.setLabel("Unset always on top");
                    frame.setAlwaysOnTop(true);
                }
        });
        popUpMenu.add(onTopMenuItem);
        popUpMenu.add(dateFormat);
        popUpMenu.add(timeZoneMenu);
        popUpMenu.add(aboutMenuItem);

        this.add(popUpMenu);

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


        while (true) {

            frame.setTitle("Timezone: " + timeZoneStr);

            repaint();

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }
    }

    public void paint(Graphics g) {

        now_zoned = now_zoned.now(TimeZone.getTimeZone(timeZoneStr).toZoneId());
        Date date = new Date();
        //SimpleDateFormat fmt = new SimpleDateFormat("dd MMM YYYY");
        SimpleDateFormat fmt = new SimpleDateFormat(formatString);

        height = this.getHeight();
        width = this.getWidth();
        clockD = width<=height ? (int)(0.85*width) : (int)(0.85*height);

        secondhand.setHandLength((int)(.9 * clockD/2));
        secondhand.setHandWidth(2);
        minutehand.setHandLength((int)(.7 * clockD/2));
        minutehand.setHandWidth(3);
        hourhand.setHandLength((int)(.4 * clockD/2));
        hourhand.setHandWidth(5);


        day = (double)now_zoned.getDayOfMonth();
        hour = (double)now_zoned.getHour();
        hour_12 = 0d;
        minute = (double)now_zoned.getMinute();
        second = (double)now_zoned.getSecond();

        double x1,y1,x2,y2;
        int length=0;
        int strokewidth=1;

        // deal with 24-hour clock
        hour_12 = hour;
        if(hour>12) hour_12 = hour-12d;

        g2 = (Graphics2D)g;

        //
        // draw clock shape
        //
        g.setColor(Color.WHITE);
        g.drawLine(0,0,1,1);

        // clock face
        g2.setStroke(new BasicStroke(1));
        g.setColor(Color.GRAY);
        g.fillOval((int)(width/2-clockD/2)-2, (int)(height/2-clockD/2)-2, clockD+1, clockD+1);

        //
        if (weatherImage!=null) {
            g.drawImage(weatherImage, width / 2 - weatherImage.getWidth(this) / 2, (int) (.6f * height), this);
        }

        // draw Calendar
        g.setColor(Color.WHITE);
        int fontSize = min(width/10, height/10);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
        g2.drawString(String.valueOf(fmt.format(new Date())), .5f*width-.5f*g.getFontMetrics().stringWidth(String.valueOf(fmt.format(new Date()))), .4f*height);


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
        fontSize = min(width/25, height/25);
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

        // Draw hands
        secondhand.draw();
        minutehand.draw();
        hourhand.draw();

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

    public static void main(String[] args) throws IOException {
        new Clock();

        //double a = 1d;
        //DecimalFormat df = new DecimalFormat("00");
        //System.out.println("Number formatted using DecimalFormat" + df.format(09.0));
    }

}