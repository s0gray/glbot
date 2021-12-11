package com.ogray.glc.grav;

import com.ogray.glc.math.Point;
import com.ogray.glc.math.Star;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Moments of gravitational field
 */
@Slf4j
public class Moments {
    class MomentsPar
    {
        public  double Ro;
        /**  r<Ro            r>Ro
            internal        external
          0  direct
          1                  direct
          2                  approx
          3 direct           direct
          4 direct           approx
         */
        public int mode;
        public int max_mom;

    };
    public MomentsPar par = new MomentsPar();

    double m1x,m1y;
    double m2xx,m2xy,m2yx,m2yy;

    double m3xxx,m3xxy,m3xyx,m3xyy;
    double m3yxx,m3yxy,m3yyx,m3yyy;

    double m4xxxx,m4xxxy,m4xxyx,m4xxyy;
    double m4xyxx,m4xyxy,m4xyyx,m4xyyy;
    double m4yxxx,m4yxxy,m4yxyx,m4yxyy;
    double m4yyxx,m4yyxy,m4yyyx,m4yyyy;

    boolean done;
    GravitatorsGenerator gen;
   // private Gravitators grv;

    @Setter @Getter
    Foot ins = null;

    public Moments(GravitatorsGenerator gen) {
        done = false;
        this.gen = gen;
       // this.grv = gen.grv;
        setDefaultValues();
    }

    public Gravitators getGrav() {
        return gen.grv;
    }

    void setDefaultValues()
    {
        par.Ro = 30;
        par.mode = 4;
        par.max_mom = 4;
        done = false;
    }

    public void calc() {
        log.info("moms calc mode="+this.par.mode + ", maxMom="+this.par.max_mom +", Ro="+this.par.Ro+", done="+done+", gen.par.mode="+gen.par.mode);
        if(done && gen.par.mode==GravitatorsGenerator.MODE_NO_MOTION)
             return;

        if(ins==null)
           ins = new Foot(getGrav().count,1024);
        ins.clear();

        m1x=0; m1y=0;
        m2xx=0; m2xy=0; m2yx=0; m2yy=0;
        m3xxx=0; m3xxy=0; m3xyx=0; m3xyy=0;
        m3yxx=0; m3yxy=0; m3yyx=0; m3yyy=0;

        m4xxxx=0; m4xxxy=0; m4xxyx=0; m4xxyy=0;
        m4xyxx=0; m4xyxy=0; m4xyyx=0; m4xyyy=0;
        m4yxxx=0; m4yxxy=0; m4yxyx=0; m4yxyy=0;
        m4yyxx=0; m4yyxy=0; m4yyyx=0; m4yyyy=0;

        for(int i=0; i<getGrav().getCount(); i++) {
          Star g = getGrav().getData()[i];
          double r2 = g.getR().mod();
          double r = Math.sqrt(r2);
          if(r>par.Ro)
          {
            m1x -= g.getR().getX()*g.getMass() / r2;
            m1y -= g.getR().getY()*g.getMass() / r2;

           double xy = g.getMass()*(-2*g.getR().getX()*g.getR().getY()/(r2*r2));
           m2xx += g.getMass()*(1/r2 - 2*g.getR().getX()*g.getR().getX()/(r2*r2));
           m2yy += g.getMass()*(1/r2 - 2*g.getR().getY()*g.getR().getY()/(r2*r2));
           m2xy += xy;
           m2yx += xy;

           double xxy = g.getMass()*(2*g.getR().getY()/(r2*r2)-
                   8*g.getR().getX()*g.getR().getX()*g.getR().getY()/(r2*r2*r2) );
           m3xxx += g.getMass()*(2*(g.getR().getX()+g.getR().getX()+g.getR().getX())/(r2*r2)
                   - 8*g.getR().getX()*g.getR().getX()*g.getR().getX()/(r2*r2*r2) );
           m3xxy += xxy;
           m3xyx += xxy;
           m3xyy += g.getMass()*(2*(g.getR().getX())/(r2*r2)
                   - 8*g.getR().getX()*g.getR().getY()*g.getR().getY()/(r2*r2*r2) );

           double yyx = g.getMass()*(2*(g.getR().getX())/(r2*r2)
                   - 8*g.getR().getY()*g.getR().getY()*g.getR().getX()/(r2*r2*r2));

           m3yxx += g.getMass()*(2*(g.getR().getY())/(r2*r2)
                   - 8*g.getR().getY()*g.getR().getX()*g.getR().getX()/(r2*r2*r2) );
           m3yxy += yyx;
           m3yyx += yyx;
           m3yyy += g.getMass()*(2*(g.getR().getY()+g.getR().getY()
                   +g.getR().getY())/(r2*r2) - 8*g.getR().getY()*g.getR().getY()*g.getR().getY()/(r2*r2*r2) );

           double xxxy = g.mass*(8/(r2*r2*r2)*(3*g.r.x*g.r.y) - 48/(r2*r2*r2*r2)*(g.r.x*g.r.x*g.r.x*g.r.y));
           double xxyy = g.mass*(-2/(r2*r2) + 8/(r2*r2*r2)*(g.r.x*g.r.x+g.r.y*g.r.y) - 48/(r2*r2*r2*r2)*(g.r.x*g.r.x*g.r.y*g.r.y));
           double xyxy = g.mass*(-2/(r2*r2) + 8/(r2*r2*r2)*(g.r.x*g.r.x+g.r.y*g.r.y) - 48/(r2*r2*r2*r2)*(g.r.x*g.r.x*g.r.y*g.r.y));
           double xyyy = g.mass*(8/(r2*r2*r2)*(3*g.r.x*g.r.y) - 48/(r2*r2*r2*r2)*(g.r.x*g.r.y*g.r.y*g.r.y));
           m4xxxx = g.mass*(-2*3/(r2*r2) + 8/(r2*r2*r2)*(6*g.r.x*g.r.x) - 48/(r2*r2*r2*r2)*(g.r.x*g.r.x*g.r.x*g.r.x));
           m4xxxy = xxxy;
           m4xxyx = xxxy;
           m4xxyy = xxyy;
           m4xyxx = xxxy;
           m4xyxy = xyxy;
           m4xyyx = xyxy;
           m4xyyy = xyyy;

           m4yxxx = xxxy;
           m4yxxy = xyxy;
           m4yxyx = xyxy;
           m4yxyy = xyyy;
           m4yyxx = xxyy;
           m4yyxy = xyyy;
           m4yyyx = xyyy;
           m4yyyy = g.mass*(-2*3/(r2*r2) + 8/(r2*r2*r2)*(6*g.r.y*g.r.y) -
                   48/(r2*r2*r2*r2)*(g.r.y*g.r.y*g.r.y*g.r.y));

           if(par.mode==1 || par.mode==3) ins.add(i);
           // in these modes external gravs are in calcs separately
          } else
          { // inside region
            if(par.mode!=1 && par.mode!=2)  // in 1 and 2 internal part is not in calcs
               ins.add(i);
          }
         }
    //   ins.shrink();  // if you need more RAM and more troubles uncomment this line

      //  log.info("inside: %ld outside: %ld\n", ins->items, grv->count - ins->items);
        done = true;
  }

    public Point calcField(Point a)
    {
        Point s = new Point(0,0);

        Point m2 = new Point(0,0);
        Point m3 = new Point(0,0);
        Point m4 = new Point(0,0);

        s.setX(m1x);  // m(1)
        s.setY(m1y);
        if(par.max_mom<2)
            return s;

        m2.x += a.getX()*m2xx+a.getY()*m2xy;  // m(2)
        m2.y += a.getX()*m2yx+a.getY()*m2yy;
        s.plusMe(m2);

        if(par.max_mom<3) return s;

        m3.x=0.5*(a.x*a.x*m3xxx+a.x*a.y*m3xxy+a.y*a.x*m3xyx+a.y*a.y*m3xyy); // m(3)
        m3.y=0.5*(a.x*a.x*m3yxx+a.x*a.y*m3yxy+a.y*a.x*m3yyx+a.y*a.y*m3yyy);
        s.plusMe(m3);

        if(par.max_mom<4) return s;

        m4.x = 1/6*(a.x*a.x*a.x*m4xxxx + a.x*a.x*a.y*m4xxxy + a.x*a.y*a.x*m4xxyx + a.x*a.y*a.y*m4xxyy + a.y*a.x*a.x*m4xyxx + a.y*a.x*a.y*m4xyxy + a.y*a.y*a.x*m4xyyx + a.y*a.y*a.y*m4xyyy); // m(4)
        m4.y = 1/6*(a.x*a.x*a.x*m4yxxx + a.x*a.x*a.y*m4yxxy + a.x*a.y*a.x*m4yxyx + a.x*a.y*a.y*m4yxyy + a.y*a.x*a.x*m4yyxx + a.y*a.x*a.y*m4yyxy + a.y*a.y*a.x*m4yyyx + a.y*a.y*a.y*m4yyyy);

        s.plusMe(m4);
        return s;
    }

    public boolean loadPar(String momPar) {
        return false;
    }

}
