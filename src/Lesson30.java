                  /*--.          .-"-.
                 /   o_O        / O o \
                 \_  (__\       \_ v _/
                 //   \\        //   \\
                ((     ))      ((     ))
 ��������������--""---""--����--""---""--��������������������������
 �                 |||            |||                             �
 �                  |              |                              �
 �                                                                �
 � Programmer:Abdul Bezrati                                       �
 � Program   :Nehe's 30th lesson port to JOGL                     �
 � Comments  :None                                                �
 �    _______                                                     �
 �  /` _____ `\;,    abezrati@hotmail.com                         �
 � (__(^===^)__)';,                                 ___           �
 �   /  :::  \   ,;                               /^   ^\         �
 �  |   :::   | ,;'                              ( �   � )        �
 ���'._______.'`��������������������������� --�oOo--(_)--oOo�--��*/

import javax.sound.sampled.*;

import java.awt.event.*;

import javax.swing.*;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES1;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.gl2.GLUT;

import java.awt.*;
import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class Lesson30 implements KeyListener
{

  String Path = "/home/leshey/workspace/Lesson30/bin/"; 
  initRenderer renderer;
  GLDrawable   glDrawable;
  GLCanvas     canvas;
  GLUquadric   cylinder_obj;                            //Quadratic object to render the cylinders
  Explosion    ExplosionArray[] = new Explosion[20];    //holds max 20 explosions at once
  Animator     loop;
  Cylinder     cyl1,cyl2,cyl3;                          //the 2 cylinders of the room
  JFrame       frame;
  Plane        pl1,pl2,pl3,pl4,pl5;                     //the 5 planes of the room
  GLUT         glut;

  Tuple3d      ArrayVel[] = new Tuple3d[10],            //holds velocity of balls
               ArrayPos[] = new Tuple3d[10],            //position of balls
               OldPos[]   = new Tuple3d[10],            //old position of balls
               veloc      = new Tuple3d(.5,- .1,  .5),  //initial velocity of balls
               accel      = new Tuple3d( 0,-.05,   0),  //acceleration ie. gravity of balls
               dir        = new Tuple3d( 0,   0, -10),  //initial direction of camera
               pos        = new Tuple3d( 0, -50,1000);  //initial position of cameraT

  boolean      soundLoaded,
               keys[]     = new boolean[256];           // Array Used For The Keyboard Routine
  double       Time       = .6;                         //timestep of simulation

  class        Explosion{                               //Explosion structure
                          Tuple3d _Position = new Tuple3d();
                          float   _Alpha,
                                  _Scale;
                        }

  class        Cylinder{
                          Tuple3d _Position,
                                  _Axis;
                             double _Radius;
                       }

  class        Plane{                                    //Plane structure
                      Tuple3d _Position,
                              _Normal;
                    }

  class        TRay{
                     Tuple3d _P,                         // Any point on the line
                             _V;                         // Direction of the line
                     TRay(Tuple3d _P,Tuple3d _V){
                       this._P = new Tuple3d(_P);
                       this._V = new Tuple3d(_V);
                     }

                     double dist(Tuple3d point){
                       double lambda = _V.Dot(new Tuple3d(point.x-_P.x,
                                                          point.y-_P.y,
                                                          point.z-_P.z));
                       Tuple3d point2 = new Tuple3d();
                       point2.scaleAdd(lambda,_V,_P);
                       return point.distance(point2);
                     }
                   }

  float        camera_rotation = 0,                           //holds rotation around the Y axis
               ZERO            = 1e-8f,
               spec[]          = {1.0f,1.0f, 1.0f,1.0f},      //sets specular highlight of balls
               posl[]          = {0.0f,400f, 0.0f,1.0f},      //position of ligth source
               amb2[]          = {0.3f,0.3f, 0.3f,1.0f},      //ambient of lightsource
               amb[]           = {0.2f,0.2f, 0.2f,1.0f};      //global ambient

  int          dlist,                       //stores display list
               sounds       = 1,            //hook camera on ball, and sound on/off
               texture[][]  = new int[2][2],//stores texture objects
               NrOfBalls,                   //sets the number of balls,
               xLocation,
               yLocation,
               canvasWidth,
               screenWidth,
               screenHeight,
               canvasHeight,
               hook_toball1 = 0;            //hook camera on ball, and sound on/off

  public static void main(String []args) {
    new Lesson30();
  }

  Lesson30(){
    int fullScreen = JOptionPane.showConfirmDialog(        null, "Would you like to run in fullscreen mode?",
                                                   "Fullscreen",  JOptionPane.YES_NO_OPTION);
    if(fullScreen!=0)
      JFrame.setDefaultLookAndFeelDecorated(true);

    frame          = new JFrame("Magic Room");
    screenWidth    = Toolkit.getDefaultToolkit().getScreenSize().width;
    screenHeight   = Toolkit.getDefaultToolkit().getScreenSize().height;

    switch(fullScreen){
      case 0:
        frame.setUndecorated(true);
      break;
      default:
        canvasWidth  = 640;
        canvasHeight = 480;
        xLocation    = (screenWidth  - canvasWidth )>>1;
        yLocation    = (screenHeight - canvasHeight)>>1;
        frame.setLocation(xLocation,yLocation);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(new ImageIcon(Path +"Data/icon.png").getImage());
    }

    //canvas  = GLDrawableFactory.getFactory().createGLCanvas(new GLCapabilities());
    canvas  = new GLCanvas();
    canvas.setSize(new Dimension(canvasWidth,canvasHeight));
    canvas.addGLEventListener((renderer = new initRenderer()));
    canvas.requestFocus();
    canvas.addKeyListener(this);

    frame.addKeyListener(this);
    frame.addWindowListener(new shutDownWindow());
    frame.getContentPane().add(canvas,BorderLayout.CENTER);

    if(fullScreen==0){
      GraphicsEnvironment.getLocalGraphicsEnvironment().
      getDefaultScreenDevice().setFullScreenWindow(frame);
      GraphicsEnvironment.getLocalGraphicsEnvironment().
      getDefaultScreenDevice().setDisplayMode((new DisplayMode(640, 480, 32,
                                               DisplayMode.REFRESH_RATE_UNKNOWN)));
    }
    else
      frame.pack();
    frame.setVisible(true);
  }

  public class initRenderer implements GLEventListener
  {
    public void init(GLAutoDrawable drawable){

      GL2 gl = drawable.getGL().getGL2();
      //float df[] = {100f};

      gl.glClearDepth(1.0f);                                      // Depth Buffer Setup
      gl.glEnable(GL.GL_DEPTH_TEST);                              // Enables Depth Testing
      gl.glDepthFunc(GL.GL_LEQUAL);                               // The Type Of Depth Testing To Do
      gl.glHint(GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT,GL.GL_NICEST);  // Really Nice Perspective Calculations

      gl.glClearColor(0,0,0,0);
      gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
      gl.glLoadIdentity();

      gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
      gl.glEnable(GL.GL_CULL_FACE);
      gl.glEnable(GL.GL_DEPTH_TEST);
      /*
      gl.glMaterialfv(gl.GL_FRONT,gl.GL_SPECULAR,spec);
      gl.glMaterialfv(gl.GL_FRONT,gl.GL_SHININESS,df);

      gl.glEnable(gl.GL_LIGHTING);
      gl.glLightfv(gl.GL_LIGHT0,gl.GL_POSITION,posl);
      gl.glLightfv(gl.GL_LIGHT0,gl.GL_AMBIENT,amb2);
      gl.glEnable(gl.GL_LIGHT0);

      gl.glLightModelfv(gl.GL_LIGHT_MODEL_AMBIENT,amb);
      */
      
      float[] lightAmbientValue = {0.5f, 0.5f, 0.5f, 1.0f};
      float[] lightDiffuseValue = {1.0f, 1.0f, 1.0f, 1.0f};
      float lightDiffusePosition[] = {0.0f, 0.0f, 2.0f, 1.0f};
      
      gl.glEnable(GL2.GL_LIGHTING);
      gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmbientValue, 0);
      gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDiffuseValue, 0);
      gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightDiffusePosition, 0);
      gl.glEnable(GL2.GL_LIGHT0); // Enable Light-1      
     
      gl.glEnable(GLLightingFunc.GL_COLOR_MATERIAL);
      gl.glColorMaterial(GL.GL_FRONT,GLLightingFunc.GL_AMBIENT_AND_DIFFUSE);
      
      
      
      gl.glEnable(GL.GL_BLEND);
      gl.glBlendFunc(GL.GL_SRC_ALPHA,GL.GL_ONE);

      gl.glEnable(GL.GL_TEXTURE_2D);
      LoadGLTextures(gl);

      //Construct billboarded explosion primitive as display list
      //4 quads at right angles to each other
      gl.glNewList(dlist=gl.glGenLists(1),GL2.GL_COMPILE);
      gl.glBegin(GL2GL3.GL_QUADS);
      gl.glRotatef(-45,0,1,0);
      gl.glNormal3f(0,0,1);
      gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-50,-40,0);
      gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(50,-40,0);
      gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(50,40,0);
      gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-50,40,0);
      gl.glNormal3f(0,0,-1);
      gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-50,40,0);
      gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(50,40,0);
      gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(50,-40,0);
      gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-50,-40,0);

      gl.glNormal3f(1,0,0);
      gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(0,-40,50);
      gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(0,-40,-50);
      gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(0,40,-50);
      gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(0,40,50);
      gl.glNormal3f(-1,0,0);
      gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(0,40,50);
      gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(0,40,-50);
      gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(0,-40,-50);
      gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(0,-40,50);
      gl.glEnd();
      gl.glEndList();

      LoadSound(Path + "Data/Explode.wav");
      InitVars(new GLU());
      loop  = new Animator(drawable);
      loop.start();
    }

    public void display(GLAutoDrawable drawable){
      GL2  gl  = drawable.getGL().getGL2();
      //GLU glu = drawable.getGLU();
      GLU glu = new GLU();
      int i;

      gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
      gl.glLoadIdentity();

      //set camera in hookmode
      if(hook_toball1==1){
        Tuple3d unit_followvector = ArrayVel[0];
        unit_followvector.normalize();

        glu.gluLookAt(ArrayPos[0].x+250,ArrayPos[0].y+250 ,ArrayPos[0].z,
                      ArrayPos[0].x+ArrayVel[0].x,ArrayPos[0].y+ArrayVel[0].y ,ArrayPos[0].z+ArrayVel[0].z
                      ,0,1,0);
      }
      else
        glu.gluLookAt(pos.x,pos.y,pos.z,
                      pos.x+dir.x,pos.y+dir.y,pos.z+dir.z,
                      0,1.0,0.0);

      gl.glClear(GL.GL_COLOR_BUFFER_BIT|GL.GL_DEPTH_BUFFER_BIT);
      gl.glRotatef(camera_rotation,0,1,0);

      //render balls
      for(i=0;i<NrOfBalls;i++){
        switch(i){
          case 1: gl.glColor3f(1.0f,1.0f,1.0f);
          break;
          case 2: gl.glColor3f(1.0f,1.0f,0.0f);
          break;
          case 3: gl.glColor3f(0.0f,1.0f,1.0f);
          break;
          case 4: gl.glColor3f(0.0f,1.0f,0.0f);
          break;
          case 5: gl.glColor3f(0.0f,0.0f,1.0f);
          break;
          case 6: gl.glColor3f(0.65f,0.2f,0.3f);
          break;
          case 7: gl.glColor3f(1.0f,0.0f,1.0f);
          break;
          case 8: gl.glColor3f(0.0f,0.7f,0.4f);
          break;
          default: gl.glColor3f(1.0f,0,0);
        }

        gl.glPushMatrix();
        gl.glTranslated(ArrayPos[i].x,ArrayPos[i].y,ArrayPos[i].z);
        glu.gluSphere(cylinder_obj,20,20,20);
        gl.glPopMatrix();
      }

      gl.glEnable(GL.GL_TEXTURE_2D);
      //render walls(planes) with texture
      gl.glBindTexture(GL.GL_TEXTURE_2D, texture[1][1]);
      gl.glColor3f(1, 1, 1);
      gl.glBegin(GL2GL3.GL_QUADS);
      gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(320,320,320);
      gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(320,-320,320);
      gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-320,-320,320);
      gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-320,320,320);

      gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-320,320,-320);
      gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-320,-320,-320);
      gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(320,-320,-320);
      gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(320,320,-320);

      gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(320,320,-320);
      gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(320,-320,-320);
      gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(320,-320,320);
      gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(320,320,320);

      gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-320,320,320);
      gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-320,-320,320);
      gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-320,-320,-320);
      gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-320,320,-320);
      gl.glEnd();

      //render floor (plane) with colours
      gl.glBindTexture(GL.GL_TEXTURE_2D, texture[1][0]);
      gl.glBegin(GL2GL3.GL_QUADS);
      gl.glTexCoord2f(1.0f, 0.0f);  gl.glVertex3f(-320,-320,320);
      gl.glTexCoord2f(1.0f, 1.0f);  gl.glVertex3f(320,-320,320);
      gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(320,-320,-320);
      gl.glTexCoord2f(0.0f, 0.0f);  gl.glVertex3f(-320,-320,-320);
      gl.glEnd();

      //render columns(cylinders)
      gl.glBindTexture(GL.GL_TEXTURE_2D, texture[0][0]);   // choose the texture to use.
      gl.glColor3f(.5f,.5f,.5f);
      gl.glPushMatrix();
      gl.glRotatef(90, 1,0,0);
      gl.glTranslatef(0,0,-500);
      glu.gluCylinder(cylinder_obj, 60, 60, 1000, 20, 2);
      gl.glPopMatrix();

      gl.glPushMatrix();
      gl.glTranslatef(200,-300,-500);
      glu.gluCylinder(cylinder_obj, 60, 60, 1000, 20, 2);
      gl.glPopMatrix();

      gl.glPushMatrix();
      gl.glTranslatef(-200,0,0);
      gl.glRotatef(135, 1,0,0);
      gl.glTranslatef(0,0,-500);
      glu.gluCylinder(cylinder_obj, 30, 30, 1000, 20, 2);
      gl.glPopMatrix();

      //render/blend explosions
      gl.glEnable(GL.GL_BLEND);
      gl.glDepthMask(false);
      gl.glBindTexture(GL.GL_TEXTURE_2D, texture[0][1]);

      for(i=0; i<20; i++){
        if(ExplosionArray[i]._Alpha>=0){
          gl.glPushMatrix();
          ExplosionArray[i]._Alpha-=0.01f;
          ExplosionArray[i]._Scale+=0.03f;
          gl.glColor4f(1,1,0,ExplosionArray[i]._Alpha);
          gl.glScalef(ExplosionArray[i]._Scale,ExplosionArray[i]._Scale,ExplosionArray[i]._Scale);
          gl.glTranslatef((float)ExplosionArray[i]._Position.x/ExplosionArray[i]._Scale,
                          (float)ExplosionArray[i]._Position.y/ExplosionArray[i]._Scale,
                          (float)ExplosionArray[i]._Position.z/ExplosionArray[i]._Scale);
          gl.glCallList(dlist);
          gl.glPopMatrix();
        }
      }
      gl.glDepthMask(true);
      gl.glDisable(GL.GL_BLEND);
      gl.glDisable(GL.GL_TEXTURE_2D);
      idle();
      processKeyboard();
    }
    public void reshape(GLAutoDrawable drawable,
                        int xstart,int ystart,
                        int width, int height){
      GL2 gl   = drawable.getGL().getGL2();
      GLU glu = new GLU();
      height  = (height == 0) ? 1 : height;

      gl.glViewport(0,0,width,height);
      gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
      gl.glLoadIdentity();

      glu.gluPerspective(50.0f,(float)width/height,10.f,1700.0f);
      gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
      gl.glLoadIdentity();
     }

     public void displayChanged(GLDrawable drawable,
                                boolean modeChanged,
                                boolean deviceChanged){}

	@Override
	public void dispose(GLAutoDrawable arg0) {
	
	}

  }

  /*************************************************************************************/
  /*************************************************************************************/
  /***                  Find if any of the current balls                            ****/
  /***             intersect with each other in the current timestep                ****/
  /***Returns the index of the 2 itersecting balls, the point and time of intersection */
  /*************************************************************************************/
  /*************************************************************************************/

  int FindBallCol(Tuple3d point, double[] TimePoint,
                  double[] Time2, int[] BallNr1, int[] BallNr2){

    Tuple3d RelativeVClone = new Tuple3d(),
            RelativeV      = new Tuple3d(),
            posi           = new Tuple3d();

    //double Timedummy2 =           -1;
    double Timedummy  =        10000;
    double MyTime     =            0;
    double Add        = Time2[0]/150;
    TRay rays;

    //Test all balls against eachother in 150 small steps
    for(int i=0;i<NrOfBalls-1;i++){
      for(int j=i+1;j<NrOfBalls;j++){

        RelativeV.sub(ArrayVel[i],ArrayVel[j]);
        RelativeVClone.set(RelativeV);
        RelativeVClone.normalize();
        rays   = new TRay(OldPos[i],RelativeVClone);
        MyTime = 0;

        if((rays.dist(OldPos[j])) > 40)
          continue;

        while(MyTime<Time2[0]){

          MyTime +=Add;
          posi.scaleAdd(MyTime,RelativeV,OldPos[i]);
          if(posi.distance(OldPos[j])<=40){
            point.set(posi);
            if(Timedummy>(MyTime - Add))
              Timedummy = MyTime - Add;

            BallNr1[0] = i;
            BallNr2[0] = j;
            break;
          }
        }
      }
    }

    if(Timedummy!=10000){
      TimePoint[0] = Timedummy;
      return 1;
    }
    return 0;
  }

  double fabs(double f){
    return (f>0) ?f :-f;
  }

  /*************************************************************************************/
  /*************************************************************************************/
  /***                        Main loop of the simulation                           ****/
  /***      Moves, finds the collisions and responses of the objects in the         ****/
  /***                            current time step.                                ****/
  /*************************************************************************************/
  /*************************************************************************************/

  void idle(){
    Tuple3d uveloc = new Tuple3d(),
            normal = new Tuple3d(),
            point  = new Tuple3d(),
            norm   = new Tuple3d(),
            time   = new Tuple3d(),
            Pos2   = new Tuple3d(),
            Nc     = new Tuple3d();

    double rt2,
           rt4,
           rt[]       = {0},
           lamda[]    = {10000},
           RestTime[] = {0},
           BallTime[] = {0};

    int    BallNr = 0;
    int    dummy  = 0;
    int    BallColNr1[] = {0};
    int    BallColNr2[] = {0};

    if(hook_toball1!=1){
      camera_rotation+=0.1f;
      if(camera_rotation>360)
        camera_rotation=0;
    }

    RestTime[0] = Time;
    lamda[0]    = 1000;

    //Compute velocity for next timestep using Euler equations
    for(int j=0;j<NrOfBalls;j++)
      ArrayVel[j].scaleAdd(RestTime[0],accel,ArrayVel[j]);

    //While timestep not over
    while (RestTime[0]>ZERO){

      lamda[0]=10000;   //initialize to very large value
      //For all the balls find closest intersection between balls and planes/cylinders
      for(int i=0;i<NrOfBalls;i++){
        //compute new position and distance
        OldPos[i] = new Tuple3d();
        OldPos[i].set(ArrayPos[i]);
        uveloc.set(ArrayVel[i]);
        uveloc.normalize();
        ArrayPos[i].scaleAdd(RestTime[0],ArrayVel[i],ArrayPos[i]);
        rt2= OldPos[i].distance(ArrayPos[i]);
       //Test if collision occured between ball and all 5 planes

       if(TestIntersionPlane(pl1,OldPos[i],uveloc,rt,norm)==1){
          //Find intersection time
          rt4=rt[0]*RestTime[0]/rt2;
          //if smaller than the one already stored replace and in timestep
          if(rt4<=lamda[0]){
            if(rt4<=RestTime[0]+ZERO)
              if(!((rt[0]<=ZERO)&&(uveloc.Dot(norm)>ZERO))){
                normal.set(norm);
                point.scaleAdd(rt[0],uveloc,OldPos[i]);
                lamda[0] = rt4;
                BallNr   = i;
              }
          }
        }

        if(TestIntersionPlane(pl2,OldPos[i],uveloc,rt,norm)==1){
          rt4=rt[0]*RestTime[0]/rt2;
          if(rt4<=lamda[0]){
            if(rt4<=RestTime[0]+ZERO)
              if(!((rt[0]<=ZERO)&&(uveloc.Dot(norm)>ZERO)) ){
                normal.set(norm);
                point.scaleAdd(rt[0],uveloc,OldPos[i]);
                lamda[0] = rt4;
                BallNr   = i;
                dummy    = 1;
              }
          }
        }

        if(TestIntersionPlane(pl3,OldPos[i],uveloc,rt,norm)==1){
          rt4=rt[0]*RestTime[0]/rt2;
          if(rt4<=lamda[0]){
            if(rt4<=RestTime[0]+ZERO)
              if(!((rt[0]<=ZERO)&&(uveloc.Dot(norm)>ZERO)) ){
                normal.set(norm);
                point.scaleAdd(rt[0],uveloc,OldPos[i]);
                lamda[0] = rt4;
                BallNr   = i;
              }
          }
        }

        if(TestIntersionPlane(pl4,OldPos[i],uveloc,rt,norm)==1){
          rt4=rt[0]*RestTime[0]/rt2;
          if(rt4<=lamda[0]){
            if(rt4<=RestTime[0]+ZERO)
              if(!((rt[0]<=ZERO)&&(uveloc.Dot(norm)>ZERO)) ){
                normal.set(norm);
                point.scaleAdd(rt[0],uveloc,OldPos[i]);
                lamda[0] = rt4;
                BallNr   = i;
              }
          }
        }

        if(TestIntersionPlane(pl5,OldPos[i],uveloc,rt,norm)==1){
          rt4=rt[0]*RestTime[0]/rt2;
          if(rt4<=lamda[0]){
            if(rt4<=RestTime[0]+ZERO)
              if(!((rt[0]<=ZERO)&&(uveloc.Dot(norm)>ZERO)) ){
                normal.set(norm);
                point.scaleAdd(rt[0],uveloc,OldPos[i]);
                lamda[0] = rt4;
                BallNr   = i;
              }
          }
        }

        //Now test intersection with the 3 cylinders
        if(TestIntersionCylinder(cyl1,OldPos[i],uveloc,rt,norm,Nc)==1){
          rt4=rt[0]*RestTime[0]/rt2;
          if(rt4<=lamda[0]){
            if(rt4<=RestTime[0]+ZERO)
              if(!((rt[0]<=ZERO)&&(uveloc.Dot(norm)>ZERO))){
                normal.set(norm);
                point.set(Nc);
                lamda[0]=rt4;
                BallNr = i;
              }
          }
        }

        if(TestIntersionCylinder(cyl2,OldPos[i],uveloc,rt,norm,Nc)==1){
          rt4=rt[0]*RestTime[0]/rt2;
          if(rt4<=lamda[0]){
            if(rt4<=RestTime[0]+ZERO)
              if(!((rt[0]<=ZERO)&&(uveloc.Dot(norm)>ZERO))){
                normal.set(norm);
                point.set(Nc);
                lamda[0] = rt4;
                BallNr = i;
              }
          }
        }

        if(TestIntersionCylinder(cyl3,OldPos[i],uveloc,rt,norm,Nc)==1){
          rt4 = rt[0]*RestTime[0]/rt2;
          if(rt4<=lamda[0]){
            if(rt4<=RestTime[0]+ZERO)
              if(!((rt[0]<=ZERO)&&(uveloc.Dot(norm)>ZERO))){
                normal.set(norm);
                point.set(Nc);
                lamda[0]=rt4;
                BallNr=i;
              }
          }
        }
      }

      //After all balls were teste with planes/cylinders test for collision
      //between them and replace if collision time smaller
       if(FindBallCol(Pos2,BallTime,RestTime,BallColNr1,BallColNr2)==1){
         if(sounds == 1)
           PlaySound();

         if((lamda[0]==10000) || (lamda[0]>BallTime[0])){
          RestTime[0] = RestTime[0] - BallTime[0];
          Tuple3d pb1   = new Tuple3d(),
                  pb2   = new Tuple3d(),
                  xaxis = new Tuple3d(),
                  U1x   = new Tuple3d(),
                  U1y   = new Tuple3d(),
                  U2x   = new Tuple3d(),
                  U2y   = new Tuple3d(),
                  V1x   = new Tuple3d(),
                  V1y   = new Tuple3d(),
                  V2x   = new Tuple3d(),
                  V2y   = new Tuple3d();
          double a,b;

          pb1.scaleAdd(BallTime[0],ArrayVel[BallColNr1[0]],OldPos[BallColNr1[0]]);
          pb2.scaleAdd(BallTime[0],ArrayVel[BallColNr2[0]],OldPos[BallColNr2[0]]);
          xaxis.sub(pb2,pb1);
          xaxis.normalize();

          a = xaxis.Dot(ArrayVel[BallColNr1[0]]);
          U1x.scaleAdd(a,xaxis);
          U1y.sub(ArrayVel[BallColNr1[0]],U1x);

          xaxis.sub(pb1,pb2);
          xaxis.normalize();

          b = xaxis.Dot(ArrayVel[BallColNr2[0]]);
          U2x.scaleAdd(b,xaxis);
          U2y.sub(ArrayVel[BallColNr2[0]],U2x);

          V1x.add(U1x,U2x);
          V1x.sub(new Tuple3d(U1x.x-U2x.x,U1x.y-U2x.y,U1x.z-U2x.z));
          V1x.scale(.5);

          V2x.add(U1x,U2x);
          V2x.sub(new Tuple3d(U2x.x-U1x.x,U2x.y-U1x.y,U2x.z-U1x.z));
          V2x.scale(.5);

          V1y.set(U1y);
          V2y.set(U2y);

          for(int j=0;j<NrOfBalls;j++)
            ArrayPos[j].scaleAdd(BallTime[0],ArrayVel[j],OldPos[j]);

          ArrayVel[BallColNr1[0]].add(V1x,V1y);
          ArrayVel[BallColNr2[0]].add(V2x,V2y);

          //Update explosion array
          for(int j=0;j<20;j++){
            if(ExplosionArray[j]._Alpha<=0){
              ExplosionArray[j]._Alpha=1;
              ExplosionArray[j]._Position=ArrayPos[BallColNr1[0]];
              ExplosionArray[j]._Scale=1;
              break;
            }
          }
          continue;
        }
      }
      //End of tests
      //If test occured move simulation for the correct timestep
      //and compute response for the colliding ball
      if(lamda[0]!=10000){
        RestTime[0]-=lamda[0];

        for(int j=0;j<NrOfBalls;j++)
          ArrayPos[j].scaleAdd(lamda[0],ArrayVel[j],OldPos[j]);

          rt2 = ArrayVel[BallNr].length();
          ArrayVel[BallNr].normalize();

          normal.scale(-2*normal.Dot(ArrayVel[BallNr]));
          ArrayVel[BallNr].add(normal,ArrayVel[BallNr]);
          ArrayVel[BallNr].normalize();
          ArrayVel[BallNr].scale(rt2);

        // Update explosion array
        for(int j=0;j<20;j++){
          if(ExplosionArray[j]._Alpha<=0){
            ExplosionArray[j]._Alpha=1;
            ExplosionArray[j]._Position=point;
            ExplosionArray[j]._Scale=1;
            break;
          }
        }
      }
      else RestTime[0]=0;
    }
  }

  /*************************************************************************************/
  /*************************************************************************************/
  /***                              Init Variables                                  ****/
  /*************************************************************************************/
  /*************************************************************************************/

  void InitVars(GLU glu){

    //create palnes
    pl1           = new Plane();
    pl1._Position = new Tuple3d(0,-300,0);
    pl1._Normal   = new Tuple3d(0,1,0);

    pl2           = new Plane();
    pl2._Position = new Tuple3d(300,0,0);
    pl2._Normal   = new Tuple3d(-1,0,0);

    pl3           = new Plane();
    pl3._Position = new Tuple3d(-300,0,0);
    pl3._Normal   = new Tuple3d(1,0,0);

    pl4           = new Plane();
    pl4._Position = new Tuple3d(0,0,300);
    pl4._Normal   = new Tuple3d(0,0,-1);

    pl5           = new Plane();
    pl5._Position = new Tuple3d(0,0,-300);
    pl5._Normal   = new Tuple3d(0,0,1);

    cyl1          = new Cylinder();
    cyl1._Position= new Tuple3d(0,0,0);
    cyl1._Axis    = new Tuple3d(0,1,0);
    cyl1._Radius  = 60+20;

    cyl2           = new Cylinder();
    cyl2._Position = new Tuple3d(200,-300,0);
    cyl2._Axis     = new Tuple3d(0,0,1);
    cyl2._Radius   = 60+20;

    cyl3           = new Cylinder();
    cyl3._Position = new Tuple3d(-200,0,0);
    cyl3._Axis     = new Tuple3d(0,1,1);
    cyl3._Axis.normalize();
    cyl3._Radius=30+20;

    //create quadratic object to render cylinders
    cylinder_obj = glu.gluNewQuadric();
    glu.gluQuadricTexture(cylinder_obj, true);

    //Set initial positions and velocities of balls
    //also initialize array which holds explosions
    NrOfBalls=10;
    ArrayVel[0] = new Tuple3d(veloc);
    ArrayPos[0] = new Tuple3d(199,180,10);

    ExplosionArray[0]  = new Explosion();
    ExplosionArray[0]._Alpha=0;
    ExplosionArray[0]._Scale=1;
    ArrayVel[1] = new Tuple3d(veloc);
    ArrayPos[1] = new Tuple3d(0,150,100);

    ExplosionArray[1]  = new Explosion();
    ExplosionArray[1]._Alpha=0;
    ExplosionArray[1]._Scale=1;
    ArrayVel[2] = new Tuple3d(veloc);
    ArrayPos[2] = new Tuple3d(-100,180,-100);

    ExplosionArray[2]  = new Explosion();
    ExplosionArray[2]._Alpha=0;
    ExplosionArray[2]._Scale=1;

    for (int i=3; i<10; i++){
      ArrayVel[i] = new Tuple3d(veloc);
      ArrayPos[i] = new Tuple3d(-500+i*75, 300, -500+i*50);

      ExplosionArray[i]  = new Explosion();
      ExplosionArray[i]._Alpha=0;
      ExplosionArray[i]._Scale=1;
    }

    for(int i=10; i<20; i++){
      ExplosionArray[i]  = new Explosion();
      ExplosionArray[i]._Alpha=0;
      ExplosionArray[i]._Scale=1;
    }
  }

  /*************************************************************************************/
  /*************************************************************************************/
  /***                 Fast Intersection Function between ray/plane                 ****/
  /*************************************************************************************/
  /*************************************************************************************/

  int TestIntersionPlane(Plane plane,Tuple3d position,Tuple3d direction,
                         double[] lamda,Tuple3d  pNormal) {

    double DotProduct = direction.Dot(plane._Normal);
    double l2;

    //determine if ray paralle to plane
    if ((DotProduct<ZERO)&&(DotProduct>-ZERO))
      return 0;

    Tuple3d substract = new Tuple3d(plane._Position);
    substract.sub(position);
    l2=(plane._Normal.Dot(substract))/DotProduct;

    if (l2<-ZERO)
      return 0;

    pNormal.set(plane._Normal);
    lamda[0]=l2;
    return 1;
  }

  /*************************************************************************************/
  /*************************************************************************************/
  /***        Fast Intersection Function between ray/cylinder                       ****/
  /*************************************************************************************/

  int TestIntersionCylinder(Cylinder cylinder,Tuple3d position,
                            Tuple3d direction, double[] lamda,
                            Tuple3d pNormal,Tuple3d newposition){
    Tuple3d RC = new Tuple3d(),
            HB = new Tuple3d(),
            n  = new Tuple3d(),
            //D  = new Tuple3d(),
            O  = new Tuple3d();
    double d,t,s,
           ln,in,out;

    RC.sub(position,cylinder._Position);
    n.cross(direction,cylinder._Axis);

    ln = n.length();

    if((ln<ZERO)&&(ln>-ZERO) )
      return 0;

    n.normalize();
    d = fabs( RC.Dot(n) );

    if(d<=cylinder._Radius){

      O.cross(RC,cylinder._Axis);
      t =  - O.Dot(n)/ln;
      O.cross(n,cylinder._Axis);
      O.normalize();
      s = fabs(Math.sqrt(cylinder._Radius*cylinder._Radius - d*d) / direction.Dot(O));

      in=t-s;
      out=t+s;

      if(in<-ZERO){
         if(out<-ZERO) return 0;
          else lamda[0]=out;
      }
      else
        if(out<-ZERO){
          lamda[0]=in;
      }
      else
      if (in<out) lamda[0]=in;
      else lamda[0]=out;

      newposition.scaleAdd(lamda[0],direction,position);
      HB.sub(newposition,cylinder._Position);
      pNormal.scaleAdd(-HB.Dot(cylinder._Axis),cylinder._Axis,HB);
      pNormal.normalize();
      return 1;
    }
    return 0;
  }

  /*************************************************************************************/
  /*************************************************************************************/
  /****                                   Load Sound                                ****/
  /*************************************************************************************/

  AudioInputStream audioInputStream;
  DataLine.Info    info;
  AudioFormat      audioFormat;
  byte[]           audio;
  Clip             boom;
  int              size;

  void LoadSound(String filename){
    /* Load Sound*/
    try {
      audioInputStream = AudioSystem.getAudioInputStream(new File(filename));
      audioFormat      = audioInputStream.getFormat();

      size    = (int)(audioFormat.getFrameSize() * audioInputStream.getFrameLength());
      audio   = new byte[size];
      info    = new DataLine.Info(Clip.class, audioFormat, size);
      audioInputStream.read(audio, 0, size);
    }
    catch(Exception e){}
  }

  /*************************************************************************************/
  /*************************************************************************************/
  /****                                   Play Sound                                ****/
  /*************************************************************************************/

  void PlaySound(){
    try{
      boom = (Clip)AudioSystem.getLine(info);
      boom.open(audioFormat, audio, 0, size);
      boom.start();
    }
    catch(Exception a){}
  }
  /*************************************************************************************/
  /*************************************************************************************/
  /****                   Load Bitmaps And Convert To Textures                      ****/
  /*************************************************************************************/

  void LoadGLTextures(GL2 gl){
    /* Load Texture*/
    loadImage image1 = new loadImage(),
              image2 = new loadImage(),
              image3 = new loadImage(),
              image4 = new loadImage();

    image1.generateTextureInfo(Path + "Data/Marble.bmp", false);
    image2.generateTextureInfo(Path + "Data/Spark.bmp" , false);
    image3.generateTextureInfo(Path + "Data/Boden.bmp" , false);
    image4.generateTextureInfo(Path + "Data/Wand.bmp"  , false);

    /* Create Texture	*****************************************/
    gl.glGenTextures(2, texture[0],0);
    gl.glBindTexture(GL.GL_TEXTURE_2D, texture[0][0]);   /* 2d texture (x and y size)*/

    gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MAG_FILTER,GL.GL_LINEAR); /* scale linearly when image bigger than texture*/
    gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MIN_FILTER,GL.GL_LINEAR); /* scale linearly when image smalled than texture*/
    gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
    gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);

    /* 2d texture, level of detail 0 (normal), 3 components (red, green, blue), x size from image, y size from image, */
    /* border 0 (normal), rgb color data, unsigned byte data, and finally the data itself.*/
    //gl.glTexImage2D(gl.GL_TEXTURE_2D, 0, 3, image1.width, image1.height, 0, gl.GL_RGB,gl.GL_UNSIGNED_BYTE, image1.data);
    Buffer buf = ByteBuffer.wrap(image1.data);
    gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, 3, image1.width, image1.height, 0, GL.GL_RGB,GL.GL_UNSIGNED_BYTE, buf);
    /* Create Texture	******************************************/
    gl.glBindTexture(GL.GL_TEXTURE_2D, texture[0][1]);   /* 2d texture (x and y size)*/

    gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MAG_FILTER,GL.GL_LINEAR); /* scale linearly when image bigger than texture*/
    gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MIN_FILTER,GL.GL_LINEAR); /* scale linearly when image smalled than texture*/
    gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
    gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);

    /* 2d texture, level of detail 0 (normal), 3 components (red, green, blue), x size from image, y size from image, */
    /* border 0 (normal), rgb color data, unsigned byte data, and finally the data itself.*/
    //gl.glTexImage2D(gl.GL_TEXTURE_2D, 0, 3, image2.width, image2.height, 0, gl.GL_RGB,gl.GL_UNSIGNED_BYTE, image2.data);
    buf = ByteBuffer.wrap(image2.data);
    gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, 3, image2.width, image2.height, 0, GL.GL_RGB,GL.GL_UNSIGNED_BYTE, buf);
    
    /* Create Texture	********************************************/
    gl.glGenTextures(2,  texture[1],0);
    gl.glBindTexture(GL.GL_TEXTURE_2D, texture[1][0]);   /* 2d texture (x and y size)*/

    gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MAG_FILTER,GL.GL_LINEAR); /* scale linearly when image bigger than texture*/
    gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MIN_FILTER,GL.GL_LINEAR); /* scale linearly when image smalled than texture*/
    gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
    gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);

    /* 2d texture, level of detail 0 (normal), 3 components (red, green, blue), x size from image, y size from image, */
    /* border 0 (normal), rgb color data, unsigned byte data, and finally the data itself.*/
    //gl.glTexImage2D(gl.GL_TEXTURE_2D, 0, 3, image3.width, image3.height, 0, gl.GL_RGB,gl.GL_UNSIGNED_BYTE, image3.data);
    buf = ByteBuffer.wrap(image3.data);
    gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, 3, image3.width, image3.height, 0, GL.GL_RGB,GL.GL_UNSIGNED_BYTE, buf);
    /* Create Texture	*********************************************/
    gl.glBindTexture(GL.GL_TEXTURE_2D, texture[1][1]);   /* 2d texture (x and y size)*/

    gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MAG_FILTER,GL.GL_LINEAR); /* scale linearly when image bigger than texture*/
    gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MIN_FILTER,GL.GL_LINEAR); /* scale linearly when image smalled than texture*/
    gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
    gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);

    /* 2d texture, level of detail 0 (normal), 3 components (red, green, blue), x size from image, y size from image, */
    /* border 0 (normal), rgb color data, unsigned byte data, and finally the data itself.*/
    //gl.glTexImage2D(gl.GL_TEXTURE_2D, 0, 3, image4.width, image4.height, 0, gl.GL_RGB,gl.GL_UNSIGNED_BYTE, image4.data);
    buf = ByteBuffer.wrap(image4.data);
    gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, 3, image4.width, image4.height, 0, GL.GL_RGB,GL.GL_UNSIGNED_BYTE, buf);
    
    image1.destroy();
    image2.destroy();
    image3.destroy();
    image4.destroy();
  }

  public void processKeyboard(){
    if(keys[KeyEvent.VK_DOWN])    pos.z-= 10;
    if(keys[KeyEvent.VK_UP])      pos.z+= 10;
    if(keys[KeyEvent.VK_LEFT])    camera_rotation+=10;
    if(keys[KeyEvent.VK_RIGHT])   camera_rotation-=10;
  }

  public void keyReleased(KeyEvent evt){
    keys[evt.getKeyCode()] = false;
  }

  public void keyPressed (KeyEvent evt){

    keys[evt.getKeyCode()] = true;

    if(keys[KeyEvent.VK_ESCAPE]){
      loop.stop();
      System.exit(0);
    }

    if(keys[KeyEvent.VK_ADD])
      Time+=0.1;

    if(keys[KeyEvent.VK_SUBTRACT])
      Time-=0.1;

    if(keys[KeyEvent.VK_F3])
      sounds^=1;

    if(keys[KeyEvent.VK_F2]){
      hook_toball1^=1;
      camera_rotation=0;
    }
  }
  public void keyTyped   (KeyEvent evt){}
  public class shutDownWindow extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
      loop.stop();
    }
  }
}
