package MainDisplay;

import Renderer.Loader;
import Renderer.Model;
import Renderer.Renderer;
import org.joml.*;
import org.joml.Math;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import Model.Kub;
import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;



public class MainDisplay {
    private static final float FOV = (float) Math.toRadians(70.0f);
    public static float x_of= 0.0f;
    private Input input;
    private static float z_of = 0.0f;
    private static final float Z_NEAR = 0.4f;

    private static final float Z_FAR = 1000000.f;

    private static int width = 1300;
    private static int height = 768;

    private Matrix4f projectionMatrix;

    // The window handle
    public long window;

    public void run() throws Exception{
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");
        System.out.println("OS name " + System.getProperty("os.name"));
        System.out.println("OS version " + System.getProperty("os.version"));
//        System.out.println("OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));


        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(width, height, "Hello World!", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
//        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
//            if(key == GLFW_KEY_D && action == GLFW_PRESS){
//                x_of+=0.1;
//            }
//            if(key == GLFW_KEY_A && action == GLFW_PRESS){
//                x_of-=0.1;
//            }
//            if(key == GLFW_KEY_Z && action == GLFW_PRESS){
//                z_of-=0.1;
//            }
//            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
//                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
//        });
        input = new Input(window);

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);


    }

    private void loop() throws Exception {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST); //Włącz test głębokości na pikselach

        float[] positions = new float[]{
                -0.5f,  0.5f, -1.05f,
                -0.5f, -0.5f, -1.05f,
                0.5f, -0.5f, -1.05f,
                0.5f,  0.5f, -1.05f,
        };
        int[] indices = new int[]{
                0, 1, 3, 3, 1, 2,
        };
        float[] colours = new float[]{
                0.5f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f,
                0.0f, 0.0f, 0.5f,
                0.0f, 0.5f, 0.5f,
        };
        Kub cube = new Kub();

        Loader loader = new Loader();
        Renderer renderer = new Renderer();
        Model model = loader.loadToVAO(cube.getPositions(),cube.getColors(),cube.getIndecies());
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {
            glClearColor(0.1f, 0.0f, 0.0f, 0.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            float aspectRatio = (float) width / height;
            projectionMatrix = new Matrix4f().perspective(FOV, aspectRatio,
                    Z_NEAR, Z_FAR);
            renderer.render(model,projectionMatrix,window,this.input.getX_of(),z_of);
            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) throws Exception{

        new MainDisplay().run();
    }

}