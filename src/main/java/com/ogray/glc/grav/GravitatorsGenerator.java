package com.ogray.glc.grav;

import com.ogray.glc.math.MathGrav;
import com.ogray.glc.math.Point;
import com.ogray.glc.math.Star;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

/**
 * Generator of Gravitators
 */
@Slf4j
public class GravitatorsGenerator {

    public class genPar
    {
        @Setter @Getter
        public int  seed;          // seed for RND generator
        @Setter @Getter

        public Point size;          // size of area in Re
        @Setter @Getter
        public int area_type;            // type of area
  /*
    0 square
    1 polar
    2 circle
    3 elliptical
  */
  @Setter @Getter

  public int ng;     // number of stars
        @Setter @Getter

        public boolean jump;            // jump in motion ?
        @Setter @Getter

        public int mass_type;
        /*
          0 equal
          1 salpeter
        */
        @Setter @Getter

        public float mass_min;
        @Setter @Getter

        public float mass_max;
        @Setter @Getter

        public float mass_power;
        @Setter @Getter

        public double m0;            // mass of 1 star
        @Setter @Getter

        public double v;             // speed
        @Setter @Getter

        public double v_angle;
        @Setter @Getter

        public boolean check_same;
        @Setter @Getter

        public int mode;    // 0 N body 1 one star 2 double star  3 no motion
        @Setter @Getter

        public float db_ro; // for double R
        @Setter @Getter

        public float db_fi; // for double fi0
        @Setter @Getter

        public float db_T; // for double w
    };
    @Setter @Getter
    public genPar par = new genPar();

    public static final int SQUARE_TYPE = 0;
    public static final int POLAR_TYPE = 1;
    public static final int CIRCLE_TYPE = 2;
    public static final int ELLIPTICAL_TYPE = 3;

    public static final int MASS_TYPE_SAME = 0;
    public static final int MASS_TYPE_SALPETER = 1;

    public static final int MODE_N_BODY = 0;
    public static final int MODE_ONE_STAR = 1;
    public static final int MODE_DOUBLE_STAR = 2;
    public static final int MODE_NO_MOTION = 3;
    boolean loaded;

    @Setter @Getter
    public Gravitators grv;

    public GravitatorsGenerator() {
        setDefaultValues();
    }

    public GravitatorsGenerator(String fileName) {
        try {
            loadPar(fileName);
            log.info("gen loaded from ", fileName);

        } catch (IOException e) {
           // e.printStackTrace();
            log.info("gen: error in loading " + fileName);
        }
    }

    public void loadPar(String fileName) throws IOException {
        setDefaultValues();

        Properties prop = new Properties();
        InputStream input = new FileInputStream(fileName);
        prop.load(input);

        for(Object key : prop.keySet() ) {
            if(!applyParam((String)key, (String)prop.get(key))) {
                log.error("Unknown parameter name " + key);
            }
        }
    }

    // old method
    private boolean applyParam(String key, String val) {
        if( key.compareTo("sigma")==0){  par.ng = getNG(Float.parseFloat(val));   return true;    }
        if( key.compareTo( "seed")==0){  par.seed = Integer.parseInt(val);   return true;    }
        if( key.compareTo("ng")==0){  par.ng = Integer.parseInt(val);   return true;    }
        if(key.compareTo( "area_type")==0){par.area_type = Integer.parseInt(val);   return true;    }
        if(key.compareTo( "size_x")==0){ par.size.x = Double.parseDouble(val);   return true;    }
        if(key.compareTo( "size_y")==0){  par.size.y = Double.parseDouble(val);   return true;    }
        if(key.compareTo( "size")==0){  par.size.x = Double.parseDouble(val);   par.size.y = Double.parseDouble(val);   return true;    }
        if(key.compareTo("m0")==0){  par.m0 = Double.parseDouble(val);   return true;    }
        if(key.compareTo( "v")==0){  par.v = Double.parseDouble(val);   return true;    }
        if(key.compareTo( "v_angle")==0){  par.v_angle= Double.parseDouble(val);   return true; }
        if(key.compareTo( "mass_type")==0){  par.mass_type = Integer.parseInt(val);   return true;    }
        if(key.compareTo( "mass_min")==0){  par.mass_min = Float.parseFloat(val);   return true;    }
        if(key.compareTo( "mass_max")==0){  par.mass_max = Float.parseFloat(val);   return true;    }
        if(key.compareTo( "mass_power")==0){  par.mass_power = Float.parseFloat(val);   return true;    }
        if(key.compareTo( "jump")==0){  par.jump = Boolean.parseBoolean(val);   return true;    }
        if(key.compareTo( "mode")==0){  par.mode = Integer.parseInt(val);   return true;    }
        if(key.compareTo( "db_ro")==0){  par.db_ro = Float.parseFloat(val);   return true;    }
        if(key.compareTo( "db_fi")==0){  par.db_fi = Float.parseFloat(val);   return true;    }
        if(key.compareTo( "db_T")==0){  par.db_T = Float.parseFloat(val);   return true;    }
        return false;
    }

    private void setDefaultValues() {
        par.seed = 1;
        par.size = new Point(50,50);
        par.ng = 100;
        par.area_type = 0;
        par.m0 = 1;
        par.v = 0.3;
        par.v_angle = 0;
        par.mass_min = 0.1f;
        par.mass_type = 0;

        par.mass_max = 1;
        par.mass_power = -2.5f;
        par.jump = true;
        par.mode = 0;
        par.db_ro = 0.5f;
        par.db_fi = 0;
        par.db_T = 10;

        loaded = false;
        generate();

        // t = 0;
    }

    /**
     * Generate gravitators
     * @return Gravitators
     */
    public Gravitators generate() {
        log.info("Generate gravitators: " + par.ng+" , massType="+par.mass_type + ", areaType="+par.area_type);
        rnd.setSeed( par.seed );
        grv = new Gravitators(par.ng);

        double totalMass = 0;

        for(int i=0; i<par.ng; i++) {
             Star st = new Star();
             switch(par.mass_type) {
                  case MASS_TYPE_SAME:
                         st.setMass(par.m0);
                         break;
                  case MASS_TYPE_SALPETER:
                         st.setMass( getSalpeter(par.mass_min, par.mass_max, par.mass_power));
                         break;
                     default:
                         log.error("Not supported star mass distribution: "+par.mass_type);
             }
              totalMass += st.getMass();

              st.setR(getNewGravPos());
              st.getV().setX( par.v * Math.cos(par.v_angle*Math.PI/180) );
              st.getV().setY( par.v * Math.sin(par.v_angle*Math.PI/180 ));
              grv.setStar(i, st);
        }
        log.info("Total gravitators mass: "+totalMass);
        return grv;
    }

    /**
     * Salpeter mass distribution
     * @param Mmin
     * @param Mmax
     * @param pwr
     * @return
     */
    private double getSalpeter(float Mmin, float Mmax, float pwr) {
        double x = RND();
        double a1= Math.pow(Mmin,1+pwr);
        double y = Math.pow( ((Math.pow(Mmax,1+pwr)-a1)*x+a1),1./(1+pwr) );
        return y;
    }

    Point getNewGravPos() {
        Point s = new Point(par.size.getX() / 2, par.size.getY() / 2);
        Point a = new Point();
        double r, phi;

        switch (par.area_type) {
            case SQUARE_TYPE:
                a.setX(par.size.getX() * RND() - s.getX());
                a.setY( par.size.getY() * RND() - s.getY() );
                break;
            case POLAR_TYPE:
                phi = RND() * 2 * Math.PI;
                r = RND() * s.getX();
                a.setX( r * Math.cos(phi) );
                a.setY( r * Math.sin(phi) );
                break;
            case CIRCLE_TYPE:
            case ELLIPTICAL_TYPE:
                do {
                    a.setX( par.size.getX() * RND() - s.getX());
                    a.setY( par.size.getY() * RND() - s.getY());
                } while (!isInside(a));
                break;
            default:
                log.error("Not supported area type: " +par.area_type);
        }
        return a;
    }

    Random rnd = new Random();
    double RND() {
        return rnd.nextDouble();
    }

    boolean isInside(Point r)
    {
        double a = Math.sqrt(r.getX()*r.getX() + r.getY()*r.getY());
        Point s = new Point(par.size.getX()/2,par.size.getY()/2);
        double b;
        switch(par.area_type)
        {
            case SQUARE_TYPE:
                if(r.getX()>-par.size.getX()/2 && r.getY()>-par.size.getY()/2
                        && r.getX()<par.size.getX()/2 && r.getY()<par.size.getY()/2)
                    return true;
                else
                    return false;
            case POLAR_TYPE:
            case CIRCLE_TYPE:
                if(a<s.getX())
                    return true;
                else
                    return false;
            case ELLIPTICAL_TYPE:
                b = r.getX()*r.getX() / (s.getX()*s.getX())+r.getY()*r.getY()/(s.getY()*s.getY());
                if( b<1 )
                    return true;
                else
                    return false;
        }
        return false;
    }

    public void next() {
        if (par.mode == 3) return; // 3rd mode - motionless
        if (par.mode == 0 || par.mode == 1)
            for (int i = 0; i < grv.count; i++) {
                Star l = grv.data[i];
                l.r.x += l.v.x;
                l.r.y += l.v.y;
                grv.data[i] = l;
                if (!isInside(l.r) && par.jump)
                    move(i);
            }
    }

    // move star #n on other side
    void move(int n)  {
        Star l = grv.data[n];
        Point r0 = l.r;
        Point v = l.v;
        Point rho = new Point();
        rho.x = par.size.x/2; rho.y = par.size.y/2;

        //    if(isInside(l.r)) return;  // no need to move

        Point R = l.r; // result
        Point t1 = new Point() ,t2 = new Point(); // пересечения траектории с областью

        switch(par.area_type)
        {
                case 0: // sq
                {
                    double a11,a12,a21,a22;
                    Point p11 = new Point(),p12 = new Point(),p21 = new Point(),p22 = new Point();
                    boolean b11, b12, b21,b22;

                    if(v.x==0){ b11 = false; b12= false;}
                    else{
                        a11 = (rho.x-r0.x)/v.x;
                        p11.x = r0.x+a11*v.x;
                        p11.y = r0.y+a11*v.y;
                        if(p11.y>rho.y || p11.y<-rho.y) b11 = false; else b11 = true;

                        a12 = (-rho.x-r0.x)/v.x;
                        p12.x = r0.x+a12*v.x;
                        p12.y = r0.y+a12*v.y;
                        if(p12.y>rho.y || p12.y<-rho.y) b12 = false; else b12 = true;
                    }

                    if(v.y==0){ b21 = false; b22= false;}
                    else{
                        a21 = (rho.y-r0.y)/v.y;
                        p21.x = r0.x+a21*v.x;
                        p21.y = r0.y+a21*v.y;
                        if(p21.x>rho.x || p21.x<-rho.x) b21 = false; else b21 = true;

                        a22 = (-rho.y-r0.y)/v.y;
                        p22.x = r0.x+a22*v.x;
                        p22.y = r0.y+a22*v.y;
                        if(p22.x>rho.x || p22.x<-rho.x) b22 = false; else b22 = true;
                    }

                    if(b11 && b12){ t1 = p11; t2 = p12;}
                    if(b11 && b21){ t1 = p11; t2 = p21;}
                    if(b11 && b22){ t1 = p11; t2 = p22;}

                    if(b21 && b12){ t1 = p21; t2 = p12;}
                    if(b21 && b22){ t1 = p21; t2 = p22;}

                    if(b12 && b22){ t1 = p12; t2 = p22;}
                }
                break;
                case 1:
                case 2: // circle
                {
                    Point tmp;
                    double v2 = v.mod();
                    double rov = r0.mul_sc(v);
                    double ro2 = r0.mod();
                    double rho2 = rho.x*rho.x;
                    double c = ro2-rho2;
                    tmp = MathGrav.SqEq(v2,2*rov,c);
                    t1.x = r0.x+tmp.x*v.x;     t1.y = r0.y+tmp.x*v.y;
                    t2.x = r0.x+tmp.y*v.x;     t2.y = r0.y+tmp.y*v.y;
                    // точки пересечения прямой движения гравитатора и области гравитаторов
                }
                break;
                case 3: // elliptical
                {
                    double a2 = rho.x*rho.x;
                    double b2 = rho.y*rho.y;
                    Point tmp;
                    double a = v.x*v.x/a2+v.y*v.y/b2;
                    double b = r0.x*v.x/a2+r0.y*v.y/b2;
                    double c = r0.x*r0.x/a2+r0.y*r0.y/b2-1;
                    tmp = MathGrav.SqEq(a,2*b,c);
                    t1.x = r0.x+tmp.x*v.x;     t1.y = r0.y+tmp.x*v.y;
                    t2.x = r0.x+tmp.y*v.x;     t2.y = r0.y+tmp.y*v.y;
                    // точки пересечения прямой движения гравитатора и области гравитаторов
                }
                break;
        }
        Point dl1 = r0.minus(t1);
        Point dl2 = r0.minus(t2);

        double l1 = dl1.mod();
        double l2 = dl2.mod();
        if(l2>l1)
        {// l2 дальше - едем туда
                R = t2;//_math::Add(dl1,t2);
        } else
                R = t1;//_math::Add(dl2,t1);

        l.r = R;
        grv.data[n] = l;
    }


    public void rndInit() {
        if(par.seed>0)
            new Random().setSeed(par.seed);
        else
            new Random().setSeed(new Date().getTime());
    }

    int getNG(float sigma)
    {
        double sq = getArea();
        if(sq==0)
            return 0;
        double full_mass = sq*sigma/Math.PI;
        double ave_mass=1;

        double n = par.mass_power;
        double a = par.mass_min;
        double b = par.mass_max;

        switch(par.mass_type)
        {
            case 0:  ave_mass = par.m0;
                break;
            case 1:  ave_mass = (Math.pow(b,n+2)-Math.pow(a,n+2))/(n+2);
                ave_mass /= (Math.pow(b,n+1)-Math.pow(a,n+1))/(n+1);
                break;
        }

        int N = (int) (full_mass/ave_mass);
        log.info("Ng for sigma="+sigma+", massType="+par.mass_type+" is "+N);
        return N;
    }

    double getArea()
    {
        double sq = 0;
        switch(par.area_type)
        {
            case 0: sq = par.size.x * par.size.y; break;
            case 1:
            case 2: sq = Math.PI*par.size.x * par.size.y/4; break;
            case 3: sq = Math.PI*par.size.x * par.size.y/4; break;
        }
        return sq;
    }
    public void loadGravs(String grvPar) {
    }

    public boolean setParam(String key, float val) {
        switch (key) {
            case "mode": {
                par.mode = (int) val;
                return true;
            }
            case "ng": {
                par.ng = (int) val;
                return true;
            }
            case "m0": {
                par.m0 = val;
                return true;
            }

        }
        return false;
    }
}
