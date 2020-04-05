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

//TODO
// 1. convert timezones to enums

import org.json.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.Timer;

import static java.lang.Integer.*;


public class Clock extends JPanel implements Runnable {

    String version = "1.0.8";

    Graphics2D g2; // used in HourHand/MinuteHand/SecondHand classes

    // Frame
    JFrame appFrame  = new JFrame();
    JFrame sideFrame = new JFrame();

    int width = 400;
    int height = 400;
    int sideFrameWidth = 140;

    // GUI objects
    private  JPopupMenu mainPopUpMenu;
    JMenuItem onTopMenuItem; // now an instance variable
    JLabel weatherIconLabel   = new JLabel();
    JLabel winddirectionLabel = new JLabel();
    JLabel tempmaxLabel       = new JLabel();
    JLabel tempminLabel       = new JLabel();
    JLabel feelslikeLabel     = new JLabel();
    JLabel tempLabel          = new JLabel();
    JLabel windtitleLabel     = new JLabel("Wind");
    JLabel windspeedLabel     = new JLabel();

    // Time variables
    ZonedDateTime zonedDateTime;
    String zoneId = "Europe/London";
    double day;
    double hour;
    double hour_12;
    double minute;
    double second;
    Secondhand secondhand;
    Minutehand minutehand;
    Hourhand hourhand;
    String formatString = "dd MMM YYYY";

    // Weather vars
    Image weatherImage;
    String weatherDescription = "";
    String temp;
    String feels_like;
    String temp_min;
    String temp_max;
    String wind_speed;
    String wind_direction;

    // External API refresh interval
    int delta = 1000; // update interval [ms]

    // GUI config
    int clockDiameter;
    int LabelFontSize = 10;
    int infoFontSize = 15;
    int subHeadingFontSize = 18;
    int headingFontSize = 20;
    //ImageIcon tick = new ImageIcon("src/main/resources/tick2.png");
    ImageIcon tick = new ImageIcon(getClass().getResource("/tick2.png"));

    static Model model;

    // schedule weather updates
    Timer timer = new Timer("RefreshWeather");
    TimerTask weatherUpdateTask;

    public Clock() throws IOException {

        secondhand = new Secondhand(this);
        minutehand = new Minutehand(this);
        hourhand = new Hourhand(this);

        appFrame.setPreferredSize(new Dimension(width, height));
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        appFrame.add(this);
        appFrame.pack();
        appFrame.setVisible(true);
        appFrame.setResizable(true);
        appFrame.setAlwaysOnTop(false);

        width = this.getWidth();
        height = this.getHeight();

        mainPopUpMenu = new JPopupMenu();

        JMenuItem weatherMenuItem = new JMenuItem("Weather...");
        onTopMenuItem = new JMenuItem("Stay on top");
        JMenu timeZoneMenu = new JMenu("TimeZone");
        JMenu dateFormatPopupMenu = new JMenu("Date/Time format");
        JMenuItem aboutMenuItem = new JMenuItem("About...");

        JMenuItem ddMMYYYY = new JMenuItem("dd MM YYYY");
        JMenuItem HHmma = new JMenuItem("HH mm a");
        JMenuItem DDDddMMM = new JMenuItem("DDD dd MMM");
        JMenuItem HHmmssSSS = new JMenuItem("HH mm ss SSS");

        ddMMYYYY.addActionListener(e -> {
            formatString = "dd MMM YYYY";
            delta = model.getDelta(formatString);
        });
        HHmma.addActionListener(e -> {
            formatString = "HH mm a";
            delta = model.getDelta(formatString);
        });
        DDDddMMM.addActionListener(e -> {
            formatString = "E dd MMM";
            delta = model.getDelta(formatString);
        });
        HHmmssSSS.addActionListener(e -> {
            formatString = "HH mm ss SSS";
            delta = model.getDelta(formatString);
            ; // required for SSS update
        });

        dateFormatPopupMenu.add(ddMMYYYY);
        dateFormatPopupMenu.add(HHmma);
        dateFormatPopupMenu.add(DDDddMMM);
        dateFormatPopupMenu.add(HHmmssSSS);

        // Hard coded list for now
        JMenuItem London = new JMenuItem("Europe/London");
        JMenuItem Rome = new JMenuItem("Europe/Rome");
        JMenuItem newYork = new JMenuItem("New York");
        JMenuItem Sydney = new JMenuItem("Australia/Sydney");
        JMenuItem Seoul = new JMenuItem("Seoul");

        London.addActionListener(e-> {
            zoneId = "Europe/London";
            getWeatherUpdate();
        });
        Rome.addActionListener(e-> {
            zoneId = "Europe/Rome";
            getWeatherUpdate();
        });
        newYork.addActionListener(e-> {
            zoneId = "EST";
            getWeatherUpdate();
        });
        Sydney.addActionListener(e-> {
            zoneId = "Australia/Sydney";
            getWeatherUpdate();
        });
        Seoul.addActionListener(e-> {
            zoneId = "Asia/Seoul";
            getWeatherUpdate();
        });

        aboutMenuItem.addActionListener(e-> {
            JOptionPane.showMessageDialog(this, "Version " + version);
        });

        timeZoneMenu.add(newYork);
        timeZoneMenu.add(London);
        timeZoneMenu.add(Rome);
        timeZoneMenu.add(Sydney);
        timeZoneMenu.add(Seoul);

        weatherMenuItem.addActionListener(e-> {
            if(sideFrame.isVisible()) {
                sideFrame.setVisible(false);
                weatherUpdateTask.cancel();

            } else {

                // Schedule a weather update every 10 mins (as per openweathermap requests)
                weatherUpdateTask = new TimerTask() {
                    @Override
                    public void run() {
                        getWeatherUpdate();
                    }
                };

                long freq = model.getFreq(); // 10min intervals tp poll openweathermap
                timer.schedule(weatherUpdateTask, 0L, freq);

                int x_position =0;

                if (appFrame.getX() + appFrame.getWidth() + sideFrameWidth < Toolkit.getDefaultToolkit().getScreenSize().width) {
                    x_position = appFrame.getX() + appFrame.getWidth();
                } else {
                    x_position = appFrame.getX() - sideFrameWidth;
                }
                if (sideFrame.getX() < 0) {
                    x_position = appFrame.getX() + appFrame.getWidth();
                }

                sideFrame.setLocation(new Point(x_position, appFrame.getY() + (appFrame.getHeight() - appFrame.getContentPane().getHeight())));
                sideFrame.setVisible(true);
            }
        });

        // toggle on top flag
        onTopMenuItem.addActionListener(e-> {
            if (appFrame.isAlwaysOnTop()) {
                onTopMenuItem.setIcon(null);
                appFrame.setAlwaysOnTop(false);
                sideFrame.setAlwaysOnTop(false);
            } else {
                onTopMenuItem.setIcon(tick);
                appFrame.setAlwaysOnTop(true);
                sideFrame.setAlwaysOnTop(true);
            }
        });
        mainPopUpMenu.add(weatherMenuItem);
        mainPopUpMenu.add(onTopMenuItem);
        mainPopUpMenu.add(dateFormatPopupMenu);
        mainPopUpMenu.add(timeZoneMenu);
        mainPopUpMenu.add(aboutMenuItem);

        this.setComponentPopupMenu(mainPopUpMenu);

        // Weather side panel display labels
        JLabel WeatherTitleLabel = new JLabel("Weather");
        JLabel tempLbl           = new JLabel("Temp:");
        JLabel feelslikeLbl      = new JLabel("Feels like:");
        JLabel tempminLbl        = new JLabel("Min:");
        JLabel tempmaxLbl        = new JLabel("Max:");
        JLabel windspeedLbl      = new JLabel("Speed:");
        JLabel winddirectionLbl  = new JLabel("Direction:");

        {
            {
                WeatherTitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, headingFontSize));
                WeatherTitleLabel.setForeground(Color.WHITE);
                WeatherTitleLabel.setAlignmentX(LEFT_ALIGNMENT);
            }
            {
                tempLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, LabelFontSize));
                tempLbl.setForeground(Color.WHITE);
                tempLbl.setAlignmentX(LEFT_ALIGNMENT);
                tempLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, infoFontSize));
                tempLabel.setForeground(Color.WHITE);
                tempLabel.setAlignmentX(LEFT_ALIGNMENT);
                feelslikeLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, LabelFontSize));
                feelslikeLbl.setForeground(Color.WHITE);
                feelslikeLbl.setAlignmentX(LEFT_ALIGNMENT);
                feelslikeLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, infoFontSize));
                feelslikeLabel.setForeground(Color.WHITE);
                feelslikeLabel.setAlignmentX(LEFT_ALIGNMENT);
                tempminLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, LabelFontSize));
                tempminLbl.setForeground(Color.WHITE);
                tempminLbl.setAlignmentX(LEFT_ALIGNMENT);
                tempminLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, infoFontSize));
                tempminLabel.setForeground(Color.WHITE);
                tempminLabel.setAlignmentX(LEFT_ALIGNMENT);
                tempmaxLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, LabelFontSize));
                tempmaxLbl.setForeground(Color.WHITE);
                tempmaxLbl.setAlignmentX(LEFT_ALIGNMENT);
                tempmaxLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, infoFontSize));
                tempmaxLabel.setForeground(Color.WHITE);
                tempmaxLabel.setAlignmentX(LEFT_ALIGNMENT);
            }
            {
                windtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, subHeadingFontSize));
                windtitleLabel.setForeground(Color.WHITE);
                windtitleLabel.setAlignmentX(LEFT_ALIGNMENT);
                windspeedLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, LabelFontSize));
                windspeedLbl.setForeground(Color.WHITE);
                windspeedLbl.setAlignmentX(LEFT_ALIGNMENT);
                windspeedLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, infoFontSize));
                windspeedLabel.setForeground(Color.WHITE);
                windspeedLabel.setAlignmentX(LEFT_ALIGNMENT);
                winddirectionLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, LabelFontSize));
                winddirectionLbl.setForeground(Color.WHITE);
                winddirectionLbl.setAlignmentX(LEFT_ALIGNMENT);
                winddirectionLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, infoFontSize));
                winddirectionLabel.setForeground(Color.WHITE);
                winddirectionLabel.setAlignmentX(LEFT_ALIGNMENT);
            }
        }


        sideFrame.setLayout(new GridBagLayout());
        sideFrame.setUndecorated(true);

        sideFrame.getContentPane().setBackground(Color.BLACK);

        GridBagConstraints c = new GridBagConstraints();

        {
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 2;
            c.insets = new Insets(5,5,5,5);
            sideFrame.getContentPane().add(WeatherTitleLabel, c);
            c.gridx = 0;
            c.gridy = 1;
            c.anchor = GridBagConstraints.WEST;
            sideFrame.getContentPane().add(weatherIconLabel, c);
            c.gridx = 0;
            c.gridy = 2;
            c.gridwidth = 1;
            sideFrame.getContentPane().add(tempLbl, c);
            c.gridx = 1;
            c.gridy = 2;
            sideFrame.getContentPane().add(tempLabel, c);
            c.gridx = 0;
            c.gridy = 3;
            sideFrame.getContentPane().add(feelslikeLbl, c);
            c.gridx = 1;
            c.gridy = 3;
            sideFrame.getContentPane().add(feelslikeLabel, c);
            c.gridx = 0;
            c.gridy = 4;
            sideFrame.getContentPane().add(tempminLbl, c);
            c.gridx = 1;
            c.gridy = 4;
            sideFrame.getContentPane().add(tempminLabel, c);
            c.gridx = 0;
            c.gridy = 5;
            sideFrame.getContentPane().add(tempmaxLbl, c);
            c.gridx = 1;
            c.gridy = 5;
            sideFrame.getContentPane().add(tempmaxLabel, c);

            c.gridx = 0;
            c.gridy = 6;
            c.gridwidth = 2;
            sideFrame.getContentPane().add(windtitleLabel, c);
            c.gridx = 0;
            c.gridy = 7;
            c.gridwidth = 1;
            sideFrame.getContentPane().add(windspeedLbl, c);
            c.gridx = 1;
            c.gridy = 7;
            sideFrame.getContentPane().add(windspeedLabel, c);
            c.gridx = 0;
            c.gridy = 8;
            sideFrame.getContentPane().add(winddirectionLbl, c);
            c.gridx = 1;
            c.gridy = 8;
            sideFrame.getContentPane().add(winddirectionLabel, c);
        }

        sideFrame.setLocation(appFrame.getX()+ appFrame.getWidth(), appFrame.getY()+ (appFrame.getHeight()- this.getHeight()));
        sideFrame.setPreferredSize(new Dimension(sideFrameWidth, height));
        sideFrame.pack();
        sideFrame.setVisible(false);

        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                sideFrame.toFront();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                sideFrame.toFront();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                sideFrame.toFront();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        appFrame.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                sideFrame.toFront();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                sideFrame.toFront();
                Point p = appFrame.getLocation();

                int x_position = 0;

                if (appFrame.getX() + appFrame.getWidth() + sideFrameWidth < Toolkit.getDefaultToolkit().getScreenSize().width) {
                    x_position = appFrame.getX() + appFrame.getWidth();
                } else {
                    x_position = appFrame.getX() - sideFrameWidth;
                }
                if (sideFrame.getX() < 0) {
                    x_position = appFrame.getX() + appFrame.getWidth();
                }
                sideFrame.setLocation(new Point(x_position, appFrame.getY()+ (appFrame.getHeight()- appFrame.getContentPane().getHeight())));
            }

            @Override
            public void componentShown(ComponentEvent e) {
                sideFrame.setState(Frame.NORMAL);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                sideFrame.setState(Frame.ICONIFIED);
            }
        });

        Thread runner = new Thread(this);
        runner.start();

    }

    void getWeatherUpdate() {

        //System.out.println("Getting weather update");
        String weatherlocation = "London,uk";
        try {

            switch (zoneId){
                case "Europe/London":
                    weatherlocation = "london,uk";
                    break;
                case "Europe/Rome":
                    weatherlocation = "rome";
                    break;
                case "EST":
                    weatherlocation = "newyork";
                    break;
                case "Australia/Sydney":
                    weatherlocation = "sydney";
                    break;
                case "Asia/Seoul":
                    weatherlocation = "seoul";
                    break;
            }

            model.setLocation(weatherlocation);
            model.getJson_weather();
            model.getWeatherdetails();
            model.getWeatherobj();
            model.getWeathericoncode();

            weatherImage       = model.getWeatherImage();
            weatherDescription = model.getWeatherDescription();
            weatherIconLabel.setIcon(new ImageIcon(weatherImage));
            weatherIconLabel.setToolTipText(weatherDescription);

            // extract temperature data
            model.getMainObj();

            temp        = model.getTemp();
            feels_like  = model.getFeels_like();
            temp_min    = model.getTemp_min();
            temp_max    = model.getTemp_max();

            tempLabel.setText(temp+"C");
            feelslikeLabel.setText(feels_like+"C");
            tempminLabel.setText(temp_min+"C");
            tempmaxLabel.setText(temp_max+"C");

            // extract wind data
            JSONObject windobj = model.getWindObj();
            try {
                wind_speed = windobj.get("speed").toString()+ "m/s";
            } catch (Exception ex) {
                wind_speed = "N/A";
            }
            try {
                wind_direction = model.getCompassHeading(windobj.get("deg").toString());
            } catch (Exception ex) {
                wind_direction = "N/A";
            }

            windspeedLabel.setText(wind_speed);
            winddirectionLabel.setText(wind_direction);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void run() {

        // add here anything that should be done regularly but not as often as in paintComponent
        while (true) {

            appFrame.setTitle("Timezone: " + zoneId);

            revalidate();
            repaint();

            try {
                Thread.sleep(delta);
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g2 = (Graphics2D)g;

        // Calculate Date & Time
        Instant nowUtc = Instant.now();
        ZoneId zoneId = ZoneId.of(this.zoneId);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(nowUtc, zoneId);

        //zonedDateTime = zonedDateTime.now(TimeZone.getTimeZone(timeZoneStr).toZoneId());
        Date date = new Date();
        //SimpleDateFormat fmt = new SimpleDateFormat(formatString);
        DateTimeFormatter fmt2 = DateTimeFormatter.ofPattern(formatString);

        height = this.getHeight();
        width = this.getWidth();
        clockDiameter = width<=height ? (int)(0.85*width) : (int)(0.85*height);

        secondhand.setHandLength((int)(.9 * clockDiameter /2));
        secondhand.setHandWidth(2);
        minutehand.setHandLength((int)(.7 * clockDiameter /2));
        minutehand.setHandWidth(3);
        hourhand.setHandLength((int)(.4 * clockDiameter /2));
        hourhand.setHandWidth(5);

        day = (double) zonedDateTime.getDayOfMonth();
        hour = (double) zonedDateTime.getHour();
        hour_12 = 0d;
        minute = (double) zonedDateTime.getMinute();
        second = (double) zonedDateTime.getSecond();

        double x1,y1,x2,y2;
        int length=0;
        int strokewidth=1;

        // deal with 24-hour clock
        hour_12 = hour;
        if(hour>12) hour_12 = hour-12d;

        this.setBackground(Color.BLACK);

        //
        // draw clock shape
        //

        // clock face
        g2.setStroke(new BasicStroke(1));
        g.setColor(Color.GRAY);
        g.fillOval((int)(width/2- clockDiameter /2)-2, (int)(height/2- clockDiameter /2)-2, clockDiameter +1, clockDiameter +1);

        if (weatherImage!=null) {
            //g.drawImage(weatherImage, width / 2 - weatherImage.getWidth(this) / 2, (int) (.6f * height), this);
        }

        // draw Date/Time
        g.setColor(Color.WHITE);
        int fontSize = min(width/10, height/10);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
        //g2.drawString(String.valueOf(fmt.format(new Date())), .5f*width-.5f*g.getFontMetrics().stringWidth(String.valueOf(fmt.format(new Date()))), .4f*height);
        g2.drawString(String.valueOf(zonedDateTime.format(fmt2)), .5f*width-.5f*g.getFontMetrics().stringWidth(String.valueOf(zonedDateTime.format(fmt2))), .4f*height);


        // minute interval markers
        for (double phi=0; phi<60d; phi++) {
            length = 3;
            strokewidth = 1;
            if(phi%5==0) {
                length = 10;
                strokewidth=2;
            }

            x1=width / 2 + clockDiameter / 2 * Math.sin(6.28d * phi / 60d);
            y1=height / 2 - clockDiameter / 2 * Math.cos(6.28d * phi / 60d);
            x2=width / 2 + (clockDiameter / 2 - length) * Math.sin(6.28d * phi / 60d);
            y2=height / 2 - (clockDiameter / 2 - length) * Math.cos(6.28d * phi / 60d);

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
        double phi = Math.asin(2d*(double)hourStringWidth/ clockDiameter); // angle for h, m & s calcs

        double theta = Math.toRadians((second) * 360d / 60d) -phi; // offset for h
        g2.rotate(theta, (int)(width/2)-2, (int)(height/2)-2);
        g.drawString(df.format(hour)+":", (int)(width/2-hourStringWidth/2), (int)((height- clockDiameter)/2)-7);
        g2.rotate(-theta, (int)(width/2)-2, (int)(height/2)-2); //reset rotate angle

        // minute
        int minuteStringWidth = g.getFontMetrics().stringWidth(df.format(minute)+":");
        g2.rotate(Math.toRadians(second*360d/60d), (int)(width/2)-2, (int)(height/2)-2);
        g.drawString(df.format(minute)+":", (int)(width/2-minuteStringWidth/2), (int)((height- clockDiameter)/2)-7);
        g2.rotate(-Math.toRadians(second*360d/60d), (int)(width/2)-2, (int)(height/2)-2);

        // second
        int secondStringWidth = g.getFontMetrics().stringWidth(df.format(second));
        theta = Math.toRadians(second * 360d / 60d)+phi; // offset for s
        g2.rotate(theta, (int)(width/2)-2, (int)(height/2)-2);
        g.drawString(df.format(second), (int)(width/2-secondStringWidth/2), (int)((height- clockDiameter)/2)-7);
        g2.rotate(-theta, (int)(width/2)-2, (int)(height/2)-2); //reset rotate angle

        // Draw hands
        secondhand.draw();
        minutehand.draw();
        hourhand.draw();

        // centre dot
        g.setColor(Color.DARK_GRAY);
        g.fillOval((int)(width/2)-3, (int)(height/2)-3,6,6);


    }

    static{
        model = new Model();
    }

    public static void main(String... args) throws IOException {
        new Clock();
    }

}