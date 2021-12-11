package com.ogray.glc;

public class ManagerParams {
        String grvPar;
        boolean grvParSet = false;

        String genPar;
        boolean genParSet  = false;

        String momPar;
        boolean momParSet = false;

        String mapPar;
        boolean mapParSet = false;

        String outMap;
        boolean outMapSet = false;

        String outGrav;
        boolean outGravSet = false;

        String srcPar;
        boolean srcParSet = false;

        String outImg;
        boolean outImgSet = false;

        String outMom;
        boolean outMomSet = false;

        String dataPar;
        boolean dataSet = false;

        String outRes;
        boolean outResSet = false;

        String resume;
        boolean resumeSet = false;

        String manPar;
        boolean manSet  = false;

        public ManagerParams() {
        }

        public void setGrvPar(String gravPar) {
                this.grvParSet = true;
                this.grvPar = gravPar;
        }

        public void setGenPar(String genPar) {
                this.genParSet = true;
                this.genPar = genPar;
        }
}
