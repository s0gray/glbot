package com.ogray.glc;

import com.ogray.glc.grav.Field;
import com.ogray.glc.grav.GravitatorsGenerator;
import com.ogray.glc.grav.Map;
import com.ogray.glc.grav.Moments;
import com.ogray.glc.math.Data;
import com.ogray.glc.math.Res;
import com.ogray.glc.source.Source;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
public class Manager {
    // generic parameter for render session

    public class manPar
    {
        public int mode;
        public int steps;
        /*
            0 smooth
            1 rnd
            2 show 1
        */
        public boolean save_all;
        public boolean same_pic;
    //    public Field.OutImageType outType; // what on picture?  0 brightness 1 field 2 det
    //    public int base_level; // for mag.pattern
    };
    public manPar par = new manPar();

    Data db = new Data();
    double i0;

    /*
    0 smooth
    1 rnd
    2 show 1
    */

    @Setter @Getter
    GravitatorsGenerator gen = new GravitatorsGenerator();

    @Setter @Getter
    Moments moments;

    @Setter @Getter
    Map map;

    @Setter @Getter
    Source src = new Source();

    long step;
    ManagerParams ps = new ManagerParams();

    public Manager(ManagerParams p) {
        if(p==null)
            return; // initialize later

        this.ps = p;

        if(p.genParSet) {
            gen = new GravitatorsGenerator(p.genPar);
        }

        if(p.grvParSet) {
            gen.loadGravs(p.grvPar);
        } else {
            gen.generate();
        }

        moments = new Moments(gen);
        if(p.momParSet)
            if(!moments.loadPar(p.momPar)) {
                log.info("Error during loading momentums: " + p.momPar);
            } else {
                log.info("Momentums loaded from: " + p.momPar);
            }

        if(p.srcParSet) {
            if(!src.loadPar(p.srcPar)) {
                log.info("Error during loading source: " + p.srcPar);
            } else {
                log.info("Source loaded from: " + p.srcPar);
            }
        }

        /// map initialization
        map = new Map(moments, src);
        if(p.mapParSet)
        {
            if(!map.loadPar(p.mapPar)) {
                log.info("Error during loading map:" + p.mapPar);
            } else {
                log.info("Map loaded from: " + p.mapPar);
            }
        }

        map.init();

        /// man init
        setDefaultValues();
        if(p.manSet)
        {
            if(!loadPar(p.manPar)) {
                log.info("Error during loading man: %s\n", p.manPar);
            } else {
                log.info("Man loaded from: %s\n", p.manPar);
            }
        }

        if(p.resumeSet)
        {
            db = new Data(0);
            if(!db.load(p.resume))
            {
                log.info("Error during loading: " + p.resume);
            }
            ps.outRes = p.resume;
            par.steps = db.par.size;
        }else
            db = new Data(par.steps);
    }

    public void init() {
        moments = new Moments(gen);
        map = new Map(moments, src);
        map.init();

        /// man init
        setDefaultValues();
    }

    private boolean loadPar(String manPar) {
        return false;
    }

    private void setDefaultValues() {
        par.mode = 0;
        par.steps = 200;
        par.save_all = false;
        par.same_pic = false;
     //   par.outType = Field.OutImageType.eBrightness;
     //   par.base_level = 100;
    }

    public void refreshGravs()
    {
        // make new distribution
        gen.generate();
    }

    public Res calcV(Res r1, Res r2)
    {
        if(r2.r.x==0 && r2.r.y==0){
            return r1;
        }

        // calculate image speed
        Res r = new Res();
        r.v = r2.r;
        r.v.minusMe(r1.r);
        r.v.div(gen.par.v);
        r.r = r1.r;
        r.i = r1.i;
        r.it = r1.it;
        r.t = r1.t;
        r.g = r1.g;
        return r;
    }

    public Res render()
    {
        Res r = new Res();
        Date t1 = new Date(),t2;

        if(!map.par.direct) {
            moments.calc();
        }

        long sec = map.calcImage();
        log.info("Image build time = "+sec + "ms");
        //----------------//
       /* if(par.save_all && !map.par.direct)
        {
            if(par.same_pic)
            {
                sprintf(fn,"%s__.gif",ps.outImg);
            }else
            if(par.mode!=1)
                sprintf(fn,"%s_%05d.gif",ps.outImg,step);
            else
                sprintf(fn,"%s_%05da.gif",ps.outImg,step);

            if(pics==NULL)
            {
                timage *gi=map1->mom1->grv->get_image(map1->par.sizePX.x,map1->par.sizePX.y,map1->par.sizeRE.x,map1->par.sizeRE.y);
                pics=timage::combine(map1->fld->get_image(), gi); // gi // NULl
            }else
            {
                timage::combo(pics, map1->fld->get_image());
            }
            if(!par.same_pic)
            {
                pics->save(fn);
                if(pics!=NULL) pics=NULL;
            }
        }*/

        if(par.mode==1 && !map.par.direct)
        {
            map.calcV();
            map.rs.v.divide( gen.par.v );

          /*  if(par.save_all && !map1.par.direct)
            {
                sprintf(fn,"%s_%05db.gif",ps.outImg,step);
                timage *gi = map1->mom1->grv->get_image(map1->par.sizePX.x,map1->par.sizePX.y,map1->par.sizeRE.x,map1->par.sizeRE.y);
                timage *ii = timage::combine(map1->fld->get_image(), gi);
                ii->save(fn);
            }*/
        }

        t2 = new Date();
        map.rs.t = t2.getTime()-t1.getTime();
//       printf("img: %lds, step:%lds ", sec, map1->rs.t);
//       printf("step:%lds ", map1->rs.t);
        map.rs.g = gen.grv.calcGc();
        step++;

        map.rs.t = sec;
        return map.rs;
    }

    public void run()
    { // main entrance
        Res rx = new Res();
        Res r1 = new Res(), r2 = new Res(), r = new Res();
        Date t0,t1;

        step = 0;
        initGravs();
//       db->init();

        initI0();

        if(map.field.getOutType() == Field.OutImageType.eMagnification) {
            map.par.calcDet = true;
        }

        t0 = new Date();
        if(par.mode==3) // preview source mode
        {
        //    if(!ps.outImgSet) memcpy(ps.outImg,"a1",6);
        //    map.field.getImage()-.save(strcat(ps.outImg,".gif"));

            Res rx1 = corr(map.rs);
         //   printf("I: %f\n",rx.i);
        }else
        if(par.mode==2) // preview mode
        {
            r1 = render();
           // if(!ps.outImgSet) memcpy(ps.outImg,"a1",6);

        /*   timage *gi = map1->mom1->grv->get_image(map1->par.sizePX.x,map1->par.sizePX.y,map1->par.sizeRE.x,map1->par.sizeRE.y );
            timage *ii = timage::combine(map1->fld->get_image(), gi);
            ii->save(strcat(ps.outImg,".gif"));

            rx = corr(map1->rs);
            printf("I: %f ICx: %f ICy: %f\n",rx.i, rx.r.x, rx.r.y);
            */

        } else
            // rewind RND gen to it
//       for(long i=0; i<par.steps; i++)
            for(long i=db.par.items; i<par.steps; i++)
            {
                map.step = i;
                switch(par.mode)
                {
                    case 0: // smooth
                        r1 = render();
                        r = calcV(r1, r2);  // (r2-r1)/vgr
                        db.add(corr(r)); // i/I0

                        r2 = r1;
                        gen.next();
                        src.refresh(1);     // t = 1
                        break;
                    case 1: // random: Monte-Carlo
                        refreshGravs();
                        r1 = render();
                        rx = corr(map.rs);
                        db.add(rx);
                        //printf("I:%f Vx:%f Vy:%f\n",rx.i,rx.v.x, rx.v.y);
                        break;
                }

             /*   if(kbhit())
                {
                    //printf("Was pressed: %c ",getch());
                    char ch = getch();
                    switch(ch)
                    {
                        case 'q':
                        case 'Q': // stop w/o questions
                            printf("Terminating calculations.. \n");
                            stop_now();
                            return;

                        case 's':
                        case 'S': // stop
                            printf("\nReally stop calculations write now? [Y/n] ");
                            switch(getch())
                            {
                                case 10:
                                case 13:
                                case 'y':
                                case 'Y':
                                    printf("Stopping calculations.. \n");
                                    stop_now();
                                    return;
                            }
                            break;
                    }
                }*/

                log.info("done: %ld of %ld ",i+1,par.steps);
                if(i%10==0)
                {
                    t1 = new Date();
                    long passed = t1.getTime()-t0.getTime();
                    double spf = (long)passed/(i+1);
                    double left = spf*(par.steps-i);
                    long finish = (long)(t1.getTime() + left);
                  //  printf("end: %s",ctime(&finish));

                   /*if(strlen(ps.log)>0)
                    {
                        FILE *pl = fopen(ps.log,"w");
                        if(pl)
                        {
                            fprintf(pl,"done: %ld of %ld ",i+1,par.steps);
                            fprintf(pl,"end: %s",ctime(&finish));
                            fclose(pl);
                        }
                    }*/

                }
              //  printf("\n");
            }
        t1 = new Date();
       // printf("Serries finished for %ld s!\n",t1-t0);
        stopNow();
    }

    private void stopNow() {
     /*   if(ps.outResSet || ps.resumeSet)
        {
            db.save(ps.outRes);
            db.save_stat(ps.outRes);
        }
        if(par.same_pic)  pics.save(fn);
*/
    }

    private void initI0() {
        if(map.par.mode==4)
        {
            moments.calc();
            map.Rstart = src.par.r;
            map.Rcur = src.par.r;
            map.calcStartPoints();

            map.calcMainTrack(par.steps);
        }

        if(src.par.size==0)
        {
            i0 = src.par.Io;
            return;
        }
        map.par.direct = true;
        Res rs = render();
        map.par.direct = false;
        i0 = rs.i;
        //printf("Io=%f\n", i0);
    }

    private void initGravs() {
        gen.rndInit();
        gen.generate();
        for(long i=0;i<db.par.items;i++)    gen.generate();
    }

    Res corr(Res rs) {
        if(i0!=0)
            rs.i/=i0;
        return rs;
    }

    public void setParams(Persist persist) {
        src.setParameter("type", persist.getSourceType());
        src.setParameter("size", persist.getSourceSize());
        map.setParameter("mode", persist.getCalcMode());
        map.setParameter("sigma_c", persist.getSigmaC());
        map.setParameter("gamma", persist.getGamma());
        map.setParameter("sizeRE", persist.getSizeRE());
        gen.setParam("ng", persist.getNg());
        gen.setParam("m0", persist.getStarM0());
    }
}
