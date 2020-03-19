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

import java.awt.*;

public class Hourhand {
    int handlength;
    int handwidth;
    Clock clock;

    Hourhand(Clock clock){
        this.clock = clock;
    }

    public void setHandLength(int length) {
        this.handlength = length;
    }

    public void setHandWidth(int handwidth) {
        this.handwidth = handwidth;
    }

    void draw() {

        clock.g2.setStroke(new BasicStroke(handwidth));
        clock.g2.setColor(Color.RED);
        clock.g2.drawLine((int) (clock.width / 2), (int) (clock.height / 2), clock.width / 2 + (int) (handlength * Math.sin(6.28d * clock.hour_12 / 12d + 6.28d / 12d * clock.minute / 60d)), clock.height / 2 - (int) (handlength * Math.cos(6.28d * clock.hour_12 / 12d + 6.28d / 12d * clock.minute / 60d)));
    }
}
