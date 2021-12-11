package com.ogray.glc.grav;

import com.ogray.glc.math.*;
import com.ogray.glc.source.Source;
import com.ogray.glc.source.SourceType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;


@Slf4j
public class Map {
    public static final int OK = 100;
    public static final int   DET_0   =   101;
    public static final int   LEAVE_ZONE = 102;
    public static final int   MAX_STEPS  =103;
    public static final int   RASXOD    = 104;

    public class mapPar
    {
        @Setter @Getter

        public Point sizePX; // pixels
        @Setter @Getter

        public Point sizeRE; // RE
        @Setter @Getter

        public double minR2; // minimum dist to star
        @Setter @Getter

        public double gamma;
        @Setter @Getter

        public double sigmaC;
        @Setter @Getter

        public double angleGamma;
        @Setter @Getter

        public boolean direct; // no grav
        @Setter @Getter
        public int mode;
  /*
   0 full fill code FFC
   1 HFC
   2 SSD
  */
        @Setter @Getter
        public int mode2; // HFC .x
        //  0 usual
        // 1 triangle
        @Setter @Getter

        public int HFCStartLevel;
        @Setter @Getter

        public double HFCeps; // max I grad
        @Setter @Getter

        public double SSD_src_inc;  // source increment size for SSD2
        @Setter @Getter

        public boolean fastV;
        @Setter @Getter

        public int itterMode; // 0 classicla 1 Zhdanov
        public boolean calcDet;
        public int nItter; //steps
    };
    @Setter @Getter

    public mapPar par = new mapPar();
    public Res rs = new Res();
    int err_code;

    public final static int MAP_MODE_FFC = 0; // full fill code
    public final static int MAP_MODE_HFC = 1; // hierarfical
    public final static int MAP_MODE_SSD = 2; //
    public final static int MAP_MODE_ONE_GRAV = 3; //
    public final static int MAP_MODE_WITT = 4; //

    public long step;

    @Setter @Getter
    Moments moments;

    @Setter @Getter
    Source source;

    public Point Rstart;
    public Point Rcur;
    public double det_cur;
    public Field field = null;

    private Matrix2 A = new Matrix2(), E = new Matrix2(), A0 = new Matrix2();
    private Complex AA = new Complex();

    Point REpx = null;

    protected long starCounter;
    protected long eqCounter;
    @Setter @Getter
    protected long rayCounter;

    public Map(Moments moments, Source source) {
        this.moments = moments;
        this.source = source;
        setDefaultValues();
    }

    void setDefaultValues()
    {
        par.sizePX = new Point(1024, 1024);
        par.sizeRE = new Point(15, 15);

        par.mode = 1;
        par.minR2 = 1e-12;

        par.gamma = 0.;
        par.sigmaC = 0.1;
        par.angleGamma = 0;
        calcMatrixA();

        par.direct = false;

        par.HFCStartLevel = 3;
        par.HFCeps = 0.1;
        par.SSD_src_inc = 0.7;

        par.mode2 = 0;

        par.fastV = false;
        par.itterMode = 0;
        par.calcDet = false;
        par.nItter = 10;

     //   p2 = null;
      //  mt = null;
    }

    /**
     * Calculate matrix A
     */
    void calcMatrixA() {
        double K = Math.PI/180;
        double g1 = par.gamma*Math.cos(2*par.angleGamma*K);
        double g2 = par.gamma*Math.sin(2*par.angleGamma*K);

        A.a[0][0] = 1-g1-par.sigmaC;
        A.a[0][1] = g2;
        A.a[1][0] = g2;
        A.a[1][1] = 1+g1-par.sigmaC;

        E.a[0][0] = 1;
        E.a[0][1] = 0;
        E.a[1][0] = 0;
        E.a[1][1] = 1;

        AA.a = g1;
        AA.b = -g2;

        A0 = E.minus(A);
    }

    public void init() {
        this.field = new Field(par.sizePX);
        this.REpx = par.sizePX.div(par.sizeRE);
    }

    public long calcImage()
    { // now it is main enterance
        log.info("map.calcImg mode="+par.mode + " mom.grv.count=" + moments
                .getGrav().count);
      /*
        This proc build both: map and image
      */
        init();

        if(source==null) {
            log.error("no source set");
            return -1;  // we need source to build image
        }

        if(source.par.size==0) {
            par.mode2 = 1;
            log.info("switching mode2 to 1");
        }

        clear();

        Date t1 = new Date();
        calcMatrixA();
        if(par.direct)
        {   //printf("direct shooting\n");
            doFFC();
        }
        else
            switch(par.mode)
            {
                case MAP_MODE_FFC:   
                    doFFC();
                    break;
                case MAP_MODE_HFC:
                    doHFC();
                    break;
                case MAP_MODE_SSD:
                    doSSD();
                    break;
                case MAP_MODE_ONE_GRAV:
                    doOneGrav();
                    break;
                case MAP_MODE_WITT:
                    doWitt();
                    break;
                default:
                    log.error("Not supported calculation mode: "+par.mode);
            }

        Date t2 = new Date();
        calcStatistics();

        log.info("rayCounter = " + this.rayCounter);
        return (t2.getTime()-t1.getTime());
    }

    /**
     * Slowest method - shoot every ray
     */
    private void doFFC() {
        log.info("doFFC sizePX=" +par.sizePX+" ");
        for(int i=0; i<par.sizePX.getX(); i++)
            for(int j=0; j<par.sizePX.getY(); j++)
                shootRay(i,j);
    }

    /**
     * Shoot single ray
     * @param i
     * @param j
     */
    private void shootRay(int i, int j) {
        if(field.getOk(i,j))
            return;

        Point shift = ray2(i,j);
        // log.debug("[" + i +", " + j +"] shift = " + shift);
        if(source!=null) {
            field.setI(i, j, source.value(shift));
        }
        rayCounter ++;
    }

    void clear()
    {
        rayCounter = 0;
        eqCounter = 0;
        field.clear();
    }

    /**
     * Calculate image statistics
     */
    void calcStatistics()
    {
        // brightness
     /*   long I = 0;
        //image center
        Point ic = new Point(0,0);

        if(mode==3 || mode==4) return;

        if(source.getQSize()==0 && direct)
        {
            I = source.value(_point(0,0)).i();
            ic = source. ->par.r;
        }
        else
        {
            for(int i=0;i< sizePX.getX();i++)
                for(int j=0;j<sizePX.getY();j++)
                {
                    long b=fld->get_i(i,j).i();
                    if(b!=0)
                    {
                        I += b;
                        ic+= _point(i,j)*b;
                    }
                }
            if(I!=0)    ic/=I;
            ic = toRE(ic);
        }
        rs.i = I;
        rs.r = ic;
        //calc_det();

      */
    }

    public Point ray2(int i, int j) { // complex shoot with grid
        if( field.getFieldOk(i,j))
            return field.getField(i,j);

        Point a = toRE( new Point(i,j));
        Complex z = new Complex(a);

        if(par.direct)
            return a;

        Complex s = new Complex(0,0);
        if(moments.par.mode == 0 ||
                moments.par.mode == 1 ||
                moments.par.mode == 3 ||
                moments.par.mode == 4)

            for(int k=0; k<moments.getIns().items; k++)
            {
                int num = moments.ins.get(k);
                Star g = moments.getGrav().data[num];
                Complex zn = z.conjugate().minus( new Complex(g.getR()).conjugate() );
                Complex psi = new Complex(g.getMass(),0).divide( zn );
                s = s.plus( psi );
            }

        if(moments.par.mode == 2 || moments.par.mode == 4)
            s = s.plus( new Complex(moments.calcField(a)) );

        Complex tmp1 = z.conjugate().mul(AA);
        Complex tmp2 = z.mul(par.sigmaC).minus(s);
        Point shift = (z.minus(tmp1).minus(tmp2)).point();
        field.setField(i,j,shift);

        if(par.calcDet) {
            field.setDet(i,j, calcDet(i,j));
        }
        return shift;
    }

    /**
     * Convert point coordinate from pixels to RE (Einstein radius)
     * @param a
     * @return
     */
    private Point toRE(Point a) {
        Point r = new Point(a.getX()-par.sizePX.getX()/2, a.getY()-par.sizePX.getY()/2);
        r.divide(REpx); // in RE
        return r;
    }

    public void calcDet()
    {
        for(int i=0; i<par.sizePX.x; i++)
            for(int j=0; j<par.sizePX.y; j++)
            {
                Point Y1 = field.getField(i,j);
                Point X1 = new Point(i,j);
                Point Y2 = field.getField(i+1,j);
                Point X2 = new Point(i+1,j);
                Point Y3 = field.getField(i,j+1);
                Point X3 = new Point(i,j+1);

                Y1 = toPX(Y1);
                Y2 = toPX(Y2);
                Y3 = toPX(Y3);

                Point dx1 = X2.minus(X1);
                Point dy1 = Y2.minus(Y1);
                Point dx2 = X3.minus(X1);
                Point dy2 = Y3.minus(Y1);

                double a11,a22,a12,a21;
                a11 = dy1.x/dx1.x;
                a12 = dy2.x/dx2.y;

                a21 = dy1.y/dx1.x;
                a22 = dy2.y/dx2.y;

                double det = a11*a22 - a12*a21;
                field.setDet((int)Y1.x,(int)Y1.y,det);
            }
    }
    double calcDet(int i, int j) {
        if(field.getDetOk(i,j))
            return field.getDet(i,j);

        // calc det
        Matrix2 o = calcMatrix(i,j);
        double det = o.det();
        field.setDet(i,j, det);
        return det;
    }

    private Matrix2 calcMatrix(int i, int j) {
        return null;
    }

    public void calcV() {
        Point vc = new Point(0,0);
        Point r1 = rs.r;
        shiftGravs();
        calcImage();
        Point r2 = rs.r;
        rs.v = r2.minus(r1);//(mom1->grv->data[0].v.x);
    }

    private void shiftGravs() {
        for(int i=0; i< moments.ins.items; i++)
        {
            int num = moments.ins.get(i);
            moments.getGrav().data[num].r.plusMe(moments.getGrav().data[num].v);
        }
    }

    public void calcStartPoints() {
    }
    public void calcMainTrack(long steps) {
    }

    public boolean loadPar(String mapPar) {
        return false;
    }

    /**
     * Hierarchical tree mode
     */
    void doHFC()
    {
        log.info("doHFC HFCStartLevel=" + par.HFCStartLevel+ ", mode2="+par.mode2);
        int image_count=0, err_count=0;
        int err_det=0, err_zone=0, err_steps=0;
        fill_HFC_level(par.HFCStartLevel);
        int step = 1 << par.HFCStartLevel;

        if(par.mode2==0) {
            for (int l = par.HFCStartLevel; l > 0; l--)
                do_HFC_level(l);
        } else {
            for(int i=0; i<par.sizePX.x; i+=step)
                for(int j=0; j<par.sizePX.y; j+=step)
                {
                    if(proceed_cell(i,j,step)>0)
                    {
                        Point r = find_sol(i+step/2,j+step/2,step,toRE(0.5),100);
                        field.setI((int)r.x,(int)r.y, new Pix(255,255,255));
                        double amp = calc_amp(r);
                        //fld->set_i(r.x,r.y,_pix(amp,amp,amp));
                        image_count++;
                        if(err_code==OK)
                        {
                            //fld->set_i(r.x,r.y,_pix(0,255,0));
                        }else
                        {
                            if(err_code == LEAVE_ZONE)
                            {
                                //     fld->set_i(r.x,r.y,_pix(255,0,0));
                                err_zone++;
                            }
                            if(err_code == DET_0)
                            {
                                //   fld->set_i(r.x,r.y,_pix(128,128,0));
                                err_det++;
                            }
                            if(err_code == MAX_STEPS)
                            {
                                //  fld->set_i(r.x,r.y,_pix(0,128,128));
                                err_steps++;
                            }
                            err_count ++;
                        }

                    }// img

                }// for
           // printf("Found %d images\n",image_count);
           // printf("Lost %d images: leave zone %d, det=0 %d max_steps %d\n",err_count, err_zone, err_det, err_steps);
        }
    }

    double calc_amp(double re, double r)
    {
        if(r==0) return 0;
        double tmp = re/r;
        tmp = tmp*tmp*tmp*tmp;
        if(tmp==1) return 0;
        tmp = 1/(1-tmp);
        if(tmp<0) tmp=-tmp;
        return tmp;
    }

    void fill_HFC_level(int num)
    {
        int step = 1<<num;
        for(int i=0;i<par.sizePX.x;i+=step)
            for(int j=0;j<par.sizePX.y;j+=step)
                shoot_ray(i,j);
    }

    void shoot_ray(int i, int j)
    {
        if(field.getOk(i,j))
            return;
        Point shift = ray2(i,j);
        if(source!=null)
            field.setI(i,j, source.value(shift));
        rayCounter ++;
    }

    void do_HFC_level(int num)
    {
        int step = 1 << num;

        for(int i=0; i<par.sizePX.x; i+=step)
            for(int j=0; j<par.sizePX.y; j+=step) {
            Pixel p[] = new Pixel[4];

            p[0] = field.get(i,j);
            if(!p[0].ok)
            {

                p[1] = field.get(i+step, j);
                p[2] = field.get(i, j+step);
                p[3] = field.get(i+step,j+step);

                boolean res = check_cell(p, new Point(i,j),step, par.mode2);

                if(res) {
                    fill_ok(i,j,i+step-1,j+step-1,p[0].i);
                }
                else
                {
                    if(step>2 || source.par.size>0) {
                        shoot_3rays(i, j, num);
                    } else {
                  /*   _point shift1 = ray(i,j+1);
                     _point shift2 = ray(i+1,j);
                     _point shift3 = ray(i+1,j+1);
                     double m1 = shift1.mod();
                     double m2 = shift2.mod();
                     double m3 = shift3.mod();
                     // CALCULATE MAGNIFICATION!!!!
                     if(m1>m2 && m1>m3){fld->set_i(i,j+1,src->par.Io); }
                     if(m2>m1 && m2>m3){fld->set_i(i+1,j,src->par.Io); }
                     if(m3>m1 && m3>m2){fld->set_i(i+1,j+1,src->par.Io); }
                    */
                    }

                }

            }// end if not ok
        } // end for
    }

    int proceed_cell(int i, int j, int step)
    {
        Pixel p[] = new Pixel[4];

        p[0] = field.get(i,j);
        if(p[0].ok)
            return 0;

        p[1] = field.get(i+step, j);
        p[2] = field.get(i, j+step);
        p[3] = field.get(i+step,j+step);

        boolean res = check_cell(p, new Point(i,j),step, par.mode2);
        if(res){
            fill_ok(i, j,i+step-1,j+step-1,p[0].i);
            return 0; // 0 = no images found
        }
        return 1;
    }

    double toRE(double px)
    {
        return par.sizeRE.x/par.sizePX.x*px;
    }

    Point find_sol(int x0, int y0,int size, double lim, int Kmax)
    { // x0,y0 - start point, step-px - max distance
        err_code = OK;
        int k=0; double dif=0;
        Point px = new Point(x0,y0);
        Point re = toRE(px);

        do {
            Point Y = ray((int)px.x, (int)px.y);
            Matrix2 o = calcMatrix((int)px.x, (int)px.y);
            Point y = source.par.r;
            o = o.back();
            if(o.det()==0)
            {
                err_code = DET_0;
                return new Point(x0, y0);
            }
            Point dx = o.mul( y.minus(Y) );
            re.plusMe( dx );
            px = toPX(re);

            if(px.x<(x0-size/2) || px.x > (x0+size/2) ||
                    px.y<(y0-size/2) || px.y >(y0+size/2))
            {  err_code = LEAVE_ZONE;
                return new Point(x0,y0);
            }
            dif = dx.l(); k++;
            if(k>Kmax)
            {
                err_code = MAX_STEPS;
                return toPX(re);
            }
        }while(dif>lim);
        return toPX(re);
    }

    Point toPX(Point a)
    { // from RE to PX
        a.mulMe(REpx);
        Point r = new Point(a.x+par.sizePX.x/2, a.y+par.sizePX.y/2);
//       _point r(a.x-par.sizePX.x/2, a.y-par.sizePX.y/2);
        return r;
    }

    double calc_amp(Point r)
    {
        Matrix2 o = calcMatrix((int)r.x, (int)r.y);
        double det = o.det();

        if(det<0) det=-det;
        if(det==0) return 255;
        return source.par.Io/det;
    }

    boolean check_cell(Pixel []p, Point coord, int step, int mode)
    {
        boolean res = false;
        switch(mode)
        {
            case 0:  // ususal
                res = check_HFC_0(p);
                break;
            case 1:   // triangle
                res = check_HFC_1(p);
                break;
        }
        return res;
    }

    /**
     *
     * @param p Pixel[4]
     * @return
     */
    boolean check_HFC_0(Pixel[] p)
    {
        if(Math.abs((double)((p[0].i.minus( p[1].i) ).i()))>par.HFCeps) return false;
        if(Math.abs((double)((p[0].i.minus(p[2].i)  ).i()))>par.HFCeps) return false;
        if(Math.abs((double)((p[0].i.minus(p[3].i)  ).i()))>par.HFCeps) return false;
        if(Math.abs((double)((p[1].i.minus(p[2].i)  ).i()))>par.HFCeps) return false;
        if(Math.abs((double)((p[1].i.minus(p[3].i)  ).i()))>par.HFCeps) return false;
        if(Math.abs((double)((p[2].i.minus(p[3].i)  ).i()))>par.HFCeps) return false;
        return true;
    }

    void fill_ok(int a1, int b1, int a2, int b2, Pix b)
    {
        for(int i=a1; i<a2; i++)
            for(int j=b1; j<b2; j++) {
                field.setOk(i,j,true);
                field.setI(i,j,b);
            }
    }

    void fill_ok(int a1, int b1, int a2, int b2, boolean val)
    {
        for(int i=a1;i<a2;i++)
            for(int j=b1;j<b2;j++)  field.setOk(i,j,val);
    }

    void shoot_3rays(int i, int j, int level)
    {
        int step = 1<<(level-1);
        shoot_ray(i+step,j);
        shoot_ray(i+step,j+step);
        shoot_ray(i,j+step);
    }

    ////////////
    // RAY SHOOTING
    Point ray(int i, int j)
    { // shoot with grid
        Point shift = new Point(0,0);
        if( field.getFieldOk(i,j))
            return field.getField(i,j);
        Point a = toRE(new Point(i,j));

        if(par.direct) return a;
        starCounter = 0;

//    _point s(0,0);

        if(moments.par.mode == 0 ||
                moments.par.mode == 1 ||
                moments.par.mode == 3 ||
                moments.par.mode == 4)

            shift = A.mul(a).minus( sum(a) );
        field.setField(i,j,shift);

        if(par.calcDet)
        {
            field.setDet(i,j, calcDet(i,j));
        }

        return shift;
    }

    /**
     *
     * @param p Pixel[4]
     * @return
     */
    boolean check_HFC_1(Pixel []p)
    {
        Point Ya = new Point(), Yb= new Point(), Yc= new Point();
        Ya = p[2].field; //- p[3].field;
        Yb = p[0].field;// - p[3].field;
        Yc = p[1].field;// - p[3].field;

        double ab = Ya.mul(Yb);
        double bc = Yb.mul(Yc);
        double ca = Yc.mul(Ya);

        if(ab*bc>0 && ca*bc>0) return false;

//     if(ab==0 || bc==0 || ca==0) return false;

        Ya = p[2].field;
        Yb = p[3].field;
        Yc = p[1].field;

        ab = Ya.mul(Yb);
        bc = Yb.mul(Yc);
        ca = Yc.mul(Ya);

        if(ab*bc>0 && ca*bc>0) return false;
//     if(ab==0 || bc==0 || ca==0) return false;

        return true;
    }

    Point sum(Point y)
    {
        Point s = new Point(0,0);
        for(int k=0; k<moments.ins.items; k++)
        {
            int num = moments.ins.get(k);
            Star g = moments.getGrav().data[num];
            Point rr = y.minus(g.r);
            double zn = rr.mod();
            double psi = 0;
            if(zn > par.minR2 && g.mass>0)
            {
                psi = g.mass / zn;
                s.plusMe(rr.mul(psi));
            }
        }
        if(moments.par.mode == 2 || (moments.par.mode == 4))
            s.plusMe(moments.calcField(y));
        return s;
    }

    void doSSD()
    {
        if(source==null)
            return;

        // 1. build map of ok
        //1.1 render bigger source
        Source old_source = source;
        source = new Source(old_source.par.size+par.SSD_src_inc, SourceType.eFlat);

        field.clear();
        fill_HFC_ok_level2(par.HFCStartLevel);

        // 2. usual HFC
        source = old_source;

        long rays_black=0, rays_img=0;

        for(int i=0;i<par.sizePX.x;i++)
            for(int j=0;j<par.sizePX.y;j++)
            {
                if(!field.getLight(i,j))
                {
                    field.setI(i,j, new Pix(0,0,0));
                    rays_black++;
                }else
                {
                    rays_img++;
                    Point shift = ray(i,j);
                    field.setField(i,j,shift);
                    field.setI(i,j, source.value(shift));
                    rayCounter ++;
                }
            }// for
        //printf("done %f %% are black \n",
         //       (float)(100.*rays_black/(rays_black+rays_img)));

    }

    void fill_HFC_ok_level2(int level)
    {
        int step = 1<<level;
        int step2 = step/2;
        for(int i=step2;i<par.sizePX.x;i+=step)
            for(int j=step2;j<par.sizePX.y;j+=step)
            {
                Point shift = ray(i,j);
                Pix b = source.value(shift);
                if(b.i()>0.3)
                    fillLight(i-step2,j-step2,i+step2, j+step2, true);
                else
                    fillLight(i-step2,j-step2,i+step2, j+step2, false);
            }
    }

    private void fillLight(int a1, int b1, int a2, int b2, boolean val) {
        for(int i=a1;i<a2;i++)
            for(int j=b1;j<b2;j++)
                field.setLight(i,j,val);
    }

    void doOneGrav()
    {
        Point Ysrc = source.par.r;
        Point Ygr = moments.getGrav().data[0].r;
        double Re = moments.getGrav().data[0].mass;
        double Re2 = Re*Re;

        Point rr = Ygr.minus(Ysrc);
        double r = Math.sqrt(rr.mod()); // dist grav - src
        Point rr1 = rr.div( r );

        double D = Math.sqrt(r*r/4+Re2);
        double y1 = r/2+D;
        double y2 = r/2-D;

        Point Y1 = rr1.mul(y1);
        Point Y2 = rr1.mul(y2);

        double k1 = calc_amp(Re,y1-r);
        double k2 = calc_amp(Re,y2-r);

        rs.i = k1+k2;
//     rs.r = (Y1*k1+Y2*k2)/rs.i;
        rs.r = rr.mul( (-Re2)/(rr.mod()+2*Re2) );

        calcMatrixA();
        rs.r.x = A.a[0][0]*rs.r.x+A.a[0][1]*rs.r.y;
        rs.r.y = A.a[1][0]*rs.r.x+A.a[1][1]*rs.r.y;

        Point xx = toPX(Y1);
        field.setI((int)xx.x,(int)xx.y, source.value( new Point(0,0)).mul(k1) );

        xx = toPX(Y2);
        field.setI((int)xx.x, (int)xx.y, source.value( new Point(0,0)).mul(k2) );
    }

    void doWitt()
    {
    /*    double sum = 0;
        Pix a;
        Point Rnew;
        rs.r = new Point(0,0);

        Rnew = calc_next_mt_point(Rcur);
        mt[step] = Rnew;
        double det = calc_detJ(Rnew);
        double amp = 1/fabs(det);

        if( Math.sgn(det)!=sgn(det_cur))
        {
            _point cc = find_cross_point(Rcur, Rnew);
            printf("CAUSTIC CROSSED at %f %f\n", cc.x, cc.y);
            build_caustic(cc, Rnew);
            if(sgn(det)==1)
            {// from '-' to '+'
                _point ni = find_new_img(cc, Rnew);
                printf("New image at %f %f\n", ni.x, ni.y);
                put_point(ni,_pix(255,0,255));
            }

            put_point(cc,_pix(255,255,0));
            put_point(Rnew,_pix(250,0,0));

        }else
            put_point(Rnew,_pix(src->par.Io/det));

        Rcur = Rnew;
//         printf("r=%f %f ",Rcur.x, Rcur.y);
        rs.r+=Rcur*amp;
        sum+=amp;
        det_cur = det;

        rs.i = sum;
        rs.r = Rcur;
        rs.r = rs.r/sum;*/
    }

    public boolean setParameter(String key, float val) {
        switch (key) {
            case "mode": {
                par.mode = (int) val;
                return true;
            }
            case "sigma_c": {
                par.sigmaC = val;
                return true;
            }
            case "gamma": {
                par.gamma = val;
                return true;
            }
            case "sizeRE": {
                par.sizeRE = new Point(val, val);
                return true;
            }
            case "sizePX": {
                par.sizePX = new Point(val, val);
                init();
                return true;
            }

        }
        return false;
    }
}
