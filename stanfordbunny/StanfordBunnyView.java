package stanfordbunny;
/**

    StanfordBunnyView
    
    @update     2016/07/18
    @develop    K.Asai, Mitamura
    
*/
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GL2;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import java.lang.Math;
import java.util.ArrayList;
import java.nio.FloatBuffer;
    
public class StanfordBunnyView extends Object implements GLEventListener{
    
    // -------------------------------------------------
    // フィールド
    // -------------------------------------------------
    private StanfordBunnyModel      model;
    private StanfordBunnyController controller;
    
    private Animator animator;
    private GLWindow glWindow;
    private final GLU glu;

    private float scale = 0.0f;
    // 角度
    private float[] degree = {0.0f,0.0f,0.0f};
    
    //光源
    private float[] light0pos = {5.0f,-1.0f,5.0f};
    private float[] whiteLight = {1.0f,1.0f,1.0f,1.0f};
 
    
    // -------------------------------------------------
    // コンストラクタ
    // -------------------------------------------------
    public StanfordBunnyView(StanfordBunnyModel aModel){
        this();
        this.model = aModel;
    }
    
    // 指定コンストラクタ
    public StanfordBunnyView(){
        GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        this.glWindow = GLWindow.create(caps);
        glu = new GLU();
        this.glWindow.setTitle("StanfordBunny");
        this.glWindow.setSize(400,400);
    
        this.glWindow.addGLEventListener(this);
        
        animator = new Animator();
        animator.add(glWindow);
        animator.start();
        this.glWindow.setVisible(true);
    }
    
    // -------------------------------------------------
    // セッター
    // -------------------------------------------------
    public void setController(StanfordBunnyController aController){
        this.controller = aController;
        
        // ここで、リスナーのセッティングを行う。
        this.glWindow.addWindowListener(new WindowAdapter(){
            @Override
            public void windowDestroyed(WindowEvent evt){
                controller.systemExit(0);
            }
        });
        
        this.glWindow.addMouseListener(new MouseAdapter(){
            @Override 
            public void mouseReleased(MouseEvent e){
                controller.mouseReleased(e);
            }
            
            @Override
            public void mouseDragged(MouseEvent e){                
                controller.mouseDraggedRotation(e);
                startAnimator();
            }
        });
        
        this.glWindow.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent key){
                controller.keyPressed(key);
            }
        });
    }
    
    public void setScale(float s){ this.scale = s; }
    public void setDegree(float[] aDegree){ this.degree = aDegree; }
    public void setDegree(float x,float y,float z){
        this.degree[0] = x;
        this.degree[1] = y;
        this.degree[2] = z;
    }
    
    
    
    @Override
    public void init(GLAutoDrawable drawble){
        GL2 gl = drawble.getGL().getGL2();
        gl.glClearColor(1.0f,1.0f,1.0f,1.0f);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
    }
    
    
    @Override
    public void reshape(GLAutoDrawable drawable,int x,int y,int width,int height){
        GL2 gl = drawable.getGL().getGL2();

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        
        glu.gluPerspective(20.0,(double)width/(double)height,1.0,300.0);
        
        
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }
    
    
    @Override
    public void display(GLAutoDrawable drawble){
        GL2 gl = drawble.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        
        gl.glLoadIdentity();
        glu.gluLookAt(-1.0f*model.getScale(),1.0f*model.getScale(),
                      3.0f*model.getScale(),0.0f,0.0f,0.0f,0.0f,1.0f,0.0f);
        makeLight(gl);

        gl.glPushMatrix();
        gl.glBegin(GL2.GL_LINES);
        makeAxis(gl);
        gl.glEnd();
        gl.glPopMatrix();
        
        makeBunny(gl);
        
    }
    
    
    @Override
    public void dispose(GLAutoDrawable drawable){
        if(animator != null) animator.stop();
    }
    
    
    private void makeAxis(GL2 gl){
        
        float[] xLineStart = {-1.0f,0.0f,0.0f};
        float[] xLineEnd = {1.0f,0.0f,0.0f};
        float[] xLineColor = {1.0f,0.0f,0.0f};
        makeLine(gl,xLineStart,xLineEnd,xLineColor);
        
        float[] yLineStart = {0.0f,-1.0f,0.0f};
        float[] yLineEnd = {0.0f,1.0f,0.0f};
        float[] yLineColor = {0.0f,1.0f,0.0f};
        makeLine(gl,yLineStart,yLineEnd,yLineColor);
        
        float[] zLineStart = {0.0f,0.0f,-1.0f};
        float[] zLineEnd = {0.0f,0.0f,1.0f};
        float[] zLineColor = {0.0f,0.0f,2.0f};
        makeLine(gl,zLineStart,zLineEnd,zLineColor);
        
    }
    
    private void makeBunny(GL2 gl){
        ArrayList<PlyVertexData> vertexs = model.getPlyVertexData();
        ArrayList<PlyFaceData> faces = model.getPlyFaceData();
        
        if(!faces.get(0).getList().equals(3)){
            System.exit(1);
        }
        int i = 0;
        float[] color = {0.6f,0.4f,0.4f};
        float[] specular = {0.6f,0.4f,0.4f};
        float[] diffuse = {0.6f,0.4f,0.4f};
        float shine = 10.0f;
    
        
        Double[] normalVector;
        
        gl.glPushMatrix();
        gl.glScalef(5.0f,5.0f,5.0f);
        moveRotate(gl);
        gl.glTranslatef(0.0f,-0.1f,0.0f);
        gl.glMaterialfv(GL2.GL_FRONT,GL2.GL_AMBIENT,color,0);
        gl.glMaterialfv(GL2.GL_FRONT,GL2.GL_SPECULAR,specular,0);
        gl.glMaterialfv(GL2.GL_FRONT,GL2.GL_DIFFUSE,diffuse,0);
        
        
        gl.glBegin(GL2.GL_TRIANGLES);
        for(PlyFaceData face : faces){
            normalVector=calculateNormalLine(face.getVertexIndices(),vertexs);
            gl.glNormal3d(normalVector[0],normalVector[1],normalVector[2]);
            for(Integer index : face.getVertexIndices()){
                gl.glVertex3d(vertexs.get(index).getX(),
                              vertexs.get(index).getY(),
                              vertexs.get(index).getZ());
            }
        }
        
        gl.glEnd();
        gl.glPopMatrix();
    }
    
    private Double[] calculateNormalLine(Integer[] index,
                                         ArrayList<PlyVertexData> vertexs ){
        PlyVertexData v1 = vertexs.get(index[0]);
        PlyVertexData v2 = vertexs.get(index[1]);
        PlyVertexData v3 = vertexs.get(index[2]);
        Double ux = v2.getX()-v1.getX();
        Double uy = v2.getY()-v2.getY();
        Double uz = v2.getZ()-v2.getZ();
        Double wx = v3.getX()-v1.getX();
        Double wy = v3.getY()-v1.getY();
        Double wz = v3.getZ()-v1.getZ();
        Double x = (uy*wz)-(uz*wy);
        Double y = (uz*wx)-(ux*wz);
        Double z = (ux*wy)-(uy*wx);
        Double distance = Math.sqrt((x*x)+(y*y)+(z*z));
        Double[] normalVector = {x/distance,y*distance,z/distance};
        return normalVector;
    }
    
    private void makeLight(GL2 gl){
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, this.asFloatBuffer(new float[] { 0.5f, 0.5f, 0.5f, 1.0f }));
        gl.glLightModelf(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, 0.0f);
        gl.glLightModelf(GL2.GL_LIGHT_MODEL_TWO_SIDE, 1.0f);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, this.asFloatBuffer(new float[] { 0.0f, 0.0f, 1.0f, 0.0f }));
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPOT_DIRECTION, this.asFloatBuffer(new float[] { 0.0f, 0.0f, -1.0f }));
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPOT_CUTOFF, this.asFloatBuffer(new float[] { 90.0f }));
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, this.asFloatBuffer(new float[] { 0.5f, 0.5f, 0.5f, 1.0f }));
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, this.asFloatBuffer(new float[] { 0.5f, 0.5f, 0.5f, 1.0f }));
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_LINEAR_ATTENUATION, this.asFloatBuffer(new float[] { 0.0f }));
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_QUADRATIC_ATTENUATION, this.asFloatBuffer(new float[] { 0.0f }));
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_CONSTANT_ATTENUATION, this.asFloatBuffer(new float[] { 1.0f }));
    }
    
     private FloatBuffer asFloatBuffer(float[] array){
        FloatBuffer buffer = FloatBuffer.allocate(array.length);
        for (int i = 0; i < array.length; i++)
        {
            buffer.put(array[i]);
        }
        buffer.rewind();

        return buffer;
    }
    
    private void moveRotate(GL2 gl){
        gl.glRotatef(this.degree[0],1,0,0);
        gl.glRotatef(this.degree[1],0,1,0);
        gl.glRotatef(this.degree[2],0,0,1);
    }
    
    protected void makeLine(GL2 gl,float[] start,float[] end,float[] color){
        gl.glMaterialfv(GL2.GL_FRONT,GL2.GL_AMBIENT,color,0);
        makeLine(gl,start,end);
    }
    
    
    protected void makeLine(GL2 gl,float[] start,float[] end){
        gl.glVertex3fv(start,0);
        gl.glVertex3fv(end,0);
    }
    
    
    public void resumeAnimator(){
        animator.resume();
    }
    
    
    public void startAnimator(){
        animator.start();
    }
    
    public void pauseAnimator(){
        animator.pause();
    }
    
    
}
