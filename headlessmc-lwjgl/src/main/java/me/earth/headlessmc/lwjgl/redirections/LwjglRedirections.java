package me.earth.headlessmc.lwjgl.redirections;

import lombok.experimental.UtilityClass;
import me.earth.headlessmc.lwjgl.LwjglProperties;
import me.earth.headlessmc.lwjgl.api.RedirectionManager;

import java.nio.*;

import static me.earth.headlessmc.lwjgl.api.Redirection.of;

// TODO: redirect Keyboard and Mouse?
@UtilityClass
public class LwjglRedirections {
    public static final int TEXTURE_SIZE = Integer.parseInt(
        System.getProperty(LwjglProperties.TEXTURE_SIZE, "1024"));
    public static final boolean FULLSCREEN = Boolean.parseBoolean(
        System.getProperty(LwjglProperties.FULLSCREEN, "true"));
    public static final int SCREEN_WIDTH = Integer.parseInt(
        System.getProperty(LwjglProperties.SCREEN_WIDTH, "1920"));
    public static final int SCREEN_HEIGHT = Integer.parseInt(
        System.getProperty(LwjglProperties.SCREEN_HEIGHT, "1080"));
    public static final int REFRESH_RATE = Integer.parseInt(
        System.getProperty(LwjglProperties.REFRESH_RATE, "100"));
    public static final int BITS_PER_PIXEL = Integer.parseInt(
        System.getProperty(LwjglProperties.BITS_PER_PIXEL, "32"));
    public static final int JNI_VERSION = Integer.parseInt(
        System.getProperty(LwjglProperties.JNI_VERSION, "24"));
    private static final ThreadLocal<Long> CURRENT_BUFFER_SIZE =
        ThreadLocal.withInitial(() -> 0L);
    private static final long START = System.nanoTime();

    public static void register(RedirectionManager manager) {
        manager.redirect(DisplayUpdater.DESC, new DisplayUpdater());
        manager.redirect("Lorg/lwjgl/glfw/GLFW;glfwWaitEventsTimeout(D)V",
                         (obj, desc, type, args) -> {
                             Thread.sleep((long) ((double) args[0] * 1000L));
                             return null;
                         });
        manager.redirect("Lorg/lwjgl/glfw/GLFW;glfwGetTime()D",
                         (obj, desc, type, args) ->
                             (System.nanoTime() - START) / 1_000_000_000.0D);
        // TODO: check this does what it's supposed to
        manager.redirect("Lorg/lwjgl/glfw/GLFW;glfwGetFramebufferSize(J[I[I)V",
                         (obj, desc, type, args) -> {
                             int[] width = (int[]) args[1];
                             width[0] = SCREEN_WIDTH;
                             int[] height = (int[]) args[2];
                             height[0] = SCREEN_HEIGHT;
                             return null;
                         });

        manager.redirect("Lorg/lwjgl/opengl/Display;getWidth()I",
                         of(SCREEN_WIDTH));
        manager.redirect("Lorg/lwjgl/opengl/Display;getHeight()I",
                         of(SCREEN_HEIGHT));
        manager.redirect("Lorg/lwjgl/opengl/Display;isFullscreen()Z",
                         of(FULLSCREEN));

        manager.redirect("Lorg/lwjgl/DefaultSysImplementation;getJNIVersion()I",
                         of(JNI_VERSION));

        // TODO: make this configurable?
        manager.redirect("Lorg/lwjgl/opengl/Display;isActive()Z", of(true));

        manager.redirect("Lorg/lwjgl/opengl/DisplayMode;isFullscreenCapable()Z",
                         of(FULLSCREEN));
        manager.redirect("Lorg/lwjgl/opengl/DisplayMode;getWidth()I",
                         of(SCREEN_WIDTH));
        manager.redirect("Lorg/lwjgl/opengl/DisplayMode;getHeight()I",
                         of(SCREEN_HEIGHT));
        manager.redirect("Lorg/lwjgl/opengl/DisplayMode;getFrequency()I",
                         of(REFRESH_RATE));
        manager.redirect("Lorg/lwjgl/opengl/DisplayMode;getBitsPerPixel()I",
                         of(BITS_PER_PIXEL));

        manager.redirect("Lorg/lwjgl/glfw/GLFW;glfwInit()Z",
                         of(true));
        manager.redirect("Lorg/lwjgl/Sys;getVersion()Ljava/lang/String;",
                         of("HeadlessMc-Lwjgl"));

        manager.redirect("Lorg/lwjgl/Sys;getTimerResolution()J",
                         of(1000L));
        manager.redirect("Lorg/lwjgl/Sys;getTime()J", (obj, desc, type, args)
            -> System.nanoTime() / 1000000L);

        manager.redirect("Lorg/lwjgl/opengl/GL11;glGetTexLevelParameteri(III)I",
                         of(TEXTURE_SIZE));
        manager.redirect("Lorg/lwjgl/opengl/GL11;glGenLists(I)I", of(-1));

        manager.redirect(
            "Lorg/lwjgl/system/MemoryUtil$MemoryAllocator;malloc(J)J",
            of(1L));
        manager.redirect(
            "Lorg/lwjgl/system/MemoryUtil$MemoryAllocator;realloc(J)J",
            of(1L));
        manager.redirect(
            "Lorg/lwjgl/system/MemoryUtil$MemoryAllocator;calloc(J)J",
            of(1L));
        manager.redirect(
            "Lorg/lwjgl/system/MemoryUtil$MemoryAllocator;realloc(J)J",
            of(1L));
        manager.redirect(
            "Lorg/lwjgl/system/MemoryUtil$MemoryAllocator;realloc(JJ)J",
            of(1L));
        manager.redirect(
            "Lorg/lwjgl/system/MemoryUtil$MemoryAllocator;aligned_alloc(JJ)J",
            of(1L));

        // blaze3d RenderTarget
        manager.redirect("Lorg/lwjgl/opengl/GL30;glCheckFramebufferStatus(I)I",
                         of(36053));

        // blaze3d NativeImage
        manager.redirect("Lorg/lwjgl/system/MemoryUtil;nmemAlloc(J)J",
                         of(1L));

        // TODO: because MemoryUtil and the Buffers are actually being used,
        //  redirect all methods inside those to return proper Buffers?
        //  - ignore list?
        // I WISH WE COULD SUBCLASS BUFFERS WTF
        manager.redirect("Lorg/lwjgl/system/MemoryUtil;memByteBuffer(JI)" +
                             "Ljava/nio/ByteBuffer;",
                         (obj, desc, type, args) ->
                             ByteBuffer.wrap(new byte[(int) args[1]]));
        manager.redirect("Lorg/lwjgl/system/MemoryUtil;" +
                             "memAlloc(I)Ljava/nio/ByteBuffer;",
                         (obj, desc, type, args) -> ByteBuffer.wrap(
                             new byte[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/system/MemoryUtil;memRealloc" +
                             "(Ljava/nio/ByteBuffer;I)Ljava/nio/ByteBuffer;",
                         (obj, desc, type, args) -> ByteBuffer.wrap(
                             new byte[(int) args[1]]));
        manager.redirect("Lorg/lwjgl/system/MemoryStack;" +
                             "mallocInt(I)Ljava/nio/IntBuffer;",
                         (obj, desc, type, args) -> IntBuffer.wrap(
                             new int[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/BufferUtils;createIntBuffer(I)" +
                             "Ljava/nio/IntBuffer;",
                         (obj, desc, type, args) -> IntBuffer.wrap(
                             new int[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/BufferUtils;createFloatBuffer(I)" +
                             "Ljava/nio/FloatBuffer;",
                         (obj, desc, type, args) -> FloatBuffer.wrap(
                             new float[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/system/MemoryUtil;createIntBuffer(I)" +
                             "Ljava/nio/IntBuffer;",
                         (obj, desc, type, args) -> IntBuffer.wrap(
                             new int[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/system/MemoryUtil;" +
                             "memAllocFloat(I)Ljava/nio/FloatBuffer;",
                         (obj, desc, type, args) -> FloatBuffer.wrap(
                             new float[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/BufferUtils;createByteBuffer(I)" +
                             "Ljava/nio/ByteBuffer;",
                         (obj, desc, type, args) -> ByteBuffer.wrap(
                             new byte[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/system/MemoryUtil;memAllocInt(I)" +
                             "Ljava/nio/IntBuffer;",
                         (obj, desc, type, args) -> IntBuffer.wrap(
                             new int[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/system/MemoryUtil;memAllocLong(I)" +
                             "Ljava/nio/LongBuffer;",
                         (obj, desc, type, args) -> LongBuffer.wrap(
                             new long[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/system/MemoryUtil;memAllocDouble(I)" +
                             "Ljava/nio/DoubleBuffer;",
                         (obj, desc, type, args) -> DoubleBuffer.wrap(
                             new double[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/system/MemoryUtil;memAllocShort(I)" +
                             "Ljava/nio/ShortBuffer;",
                         (obj, desc, type, args) -> ShortBuffer.wrap(
                             new short[(int) args[0]]));
        manager.redirect("Lorg/lwjgl/system/MemoryStack;malloc(I)" +
                             "Ljava/nio/ByteBuffer;",
                         (obj, desc, type, args) -> ByteBuffer.wrap(
                             new byte[(int) args[0]]));

        // TODO: this is really bad...
        manager.redirect("Lorg/lwjgl/opengl/GL15;glBufferData(IJI)V",
                         (obj, desc, type, args) -> {
                             CURRENT_BUFFER_SIZE.set((Long) args[1]);
                             return null;
                         });
        manager.redirect("Lorg/lwjgl/opengl/GL15;glMapBuffer(II)" +
                             "Ljava/nio/ByteBuffer;",
                         (obj, desc, type, args) -> ByteBuffer.wrap(
                             new byte[CURRENT_BUFFER_SIZE.get().intValue()]));

        manager.redirect("Lorg/lwjgl/system/MemoryUtil;" +
                             "memAddress(Ljava/nio/ByteBuffer;)J", of(1L));

        manager.redirect(STBIImageRedirection.DESC,
                         STBIImageRedirection.INSTANCE);
        manager.redirect(MemASCIIRedirection.DESC,
                         MemASCIIRedirection.INSTANCE);

        // act as if we compiled a shader program (blaze3d program)
        manager.redirect("Lorg/lwjgl/opengl/GL20;glGetShaderi(II)I", of(1));
        manager.redirect("Lorg/lwjgl/opengl/GL20;glCreateProgram()I", of(1));
        manager.redirect("Lorg/lwjgl/opengl/GL20;glGetProgrami(II)I", of(1));

        manager.redirect("Lorg/lwjgl/openal/ALC10;alcOpenDevice(" +
                             "Ljava/lang/CharSequence;)J", of(1L));
    }

}
