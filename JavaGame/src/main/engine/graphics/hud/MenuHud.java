package main.engine.graphics.hud;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_HIDDEN;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_B;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_C;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_E;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_END;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_HOME;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_TAB;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_X;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Z;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwSetClipboardString;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.nglfwGetClipboardString;
import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11C.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.GL_NEAREST;
import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL11C.GL_RGBA8;
import static org.lwjgl.opengl.GL11C.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_SHORT;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL11C.glDeleteTextures;
import static org.lwjgl.opengl.GL11C.glDisable;
import static org.lwjgl.opengl.GL11C.glDrawElements;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL11C.glGenTextures;
import static org.lwjgl.opengl.GL11C.glScissor;
import static org.lwjgl.opengl.GL11C.glTexImage2D;
import static org.lwjgl.opengl.GL11C.glTexParameteri;
import static org.lwjgl.opengl.GL12C.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15C.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL15C.glBufferData;
import static org.lwjgl.opengl.GL15C.glDeleteBuffers;
import static org.lwjgl.opengl.GL15C.glMapBuffer;
import static org.lwjgl.opengl.GL15C.glUnmapBuffer;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.stb.STBTruetype.stbtt_GetCodepointHMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_GetFontVMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_GetPackedQuad;
import static org.lwjgl.stb.STBTruetype.stbtt_InitFont;
import static org.lwjgl.stb.STBTruetype.stbtt_PackBegin;
import static org.lwjgl.stb.STBTruetype.stbtt_PackEnd;
import static org.lwjgl.stb.STBTruetype.stbtt_PackFontRange;
import static org.lwjgl.stb.STBTruetype.stbtt_PackSetOversampling;
import static org.lwjgl.stb.STBTruetype.stbtt_ScaleForPixelHeight;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memCopy;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.nmemAllocChecked;
import static org.lwjgl.system.MemoryUtil.nmemFree;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.joml.Matrix4f;
import org.lwjgl.nuklear.NkAllocator;
import org.lwjgl.nuklear.NkBuffer;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkConvertConfig;
import org.lwjgl.nuklear.NkDrawCommand;
import org.lwjgl.nuklear.NkDrawNullTexture;
import org.lwjgl.nuklear.NkDrawVertexLayoutElement;
import org.lwjgl.nuklear.NkMouse;
import org.lwjgl.nuklear.NkUserFont;
import org.lwjgl.nuklear.NkUserFontGlyph;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;

import main.engine.MouseInput;
import main.engine.Window;
import main.engine.graphics.IHud;
import main.engine.graphics.IHudElement;
import main.engine.graphics.Transformation;
import main.engine.graphics.opengl.ShaderProgram;
import main.engine.utility.Utils;

public class MenuHud implements IHud {
	
	public static final int MAX_ELEMENTS = 10;
	
	protected final int vaoId, vboId, eboId;

    private final ByteBuffer ttf;
    
    private NkContext ctx = NkContext.create();
	
	private static final NkAllocator ALLOCATOR;

    private static final NkDrawVertexLayoutElement.Buffer VERTEX_LAYOUT;
    
    static {
        ALLOCATOR = NkAllocator.create()
            .alloc((handle, old, size) -> nmemAllocChecked(size))
            .mfree((handle, ptr) -> nmemFree(ptr));

        VERTEX_LAYOUT = NkDrawVertexLayoutElement.create(4)
            .position(0).attribute(NK_VERTEX_POSITION).format(NK_FORMAT_FLOAT).offset(0)
            .position(1).attribute(NK_VERTEX_TEXCOORD).format(NK_FORMAT_FLOAT).offset(8)
            .position(2).attribute(NK_VERTEX_COLOR).format(NK_FORMAT_R8G8B8A8).offset(16)
            .position(3).attribute(NK_VERTEX_ATTRIBUTE_COUNT).format(NK_FORMAT_COUNT).offset(0)
            .flip();
    }
    
    private NkUserFont default_font = NkUserFont.create();

    private NkBuffer          cmds         = NkBuffer.create();
    private NkDrawNullTexture null_texture = NkDrawNullTexture.create();
    
    private IHudElement[] elements;
    
    private boolean AA;
    
    private final ShaderProgram shader;
    
    private final Demo       demo = new Demo();
    private final Calculator calc = new Calculator();
    
    private final Transformation transformation;

    /*public Hud(String statusText) throws Exception {
    }*/
    
    public MenuHud(Window window) throws Exception {
    	transformation = new Transformation();
    	elements = new IHudElement[MAX_ELEMENTS];
    	AA = true;
    	try {
            this.ttf = Utils.ioResourceToByteBuffer(System.getProperty("user.dir") + "\\src\\main\\resources\\fonts\\FiraSans-Regular.ttf", 512 * 1024);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    	
    	ctx = setupWindow(window.getWindowHandle());
        
    	nk_buffer_init(cmds, ALLOCATOR, BUFFER_INITIAL_SIZE);
    	shader = new ShaderProgram();
    	setupShader();
    	
    	vboId = glGenBuffers();
    	eboId = glGenBuffers();
    	vaoId = glGenVertexArrays();
    	
    	glBindVertexArray(vaoId);
    	glBindBuffer(GL_ARRAY_BUFFER, vboId);
    	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
    	
    	glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
    	
    	glVertexAttribPointer(0, 2, GL_FLOAT, false, 20, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 20, 8);
        glVertexAttribPointer(2, 4, GL_UNSIGNED_BYTE, true, 20, 16);
        
        // null texture setup
        int nullTexID = glGenTextures();

        null_texture.texture().id(nullTexID);
        null_texture.uv().set(0.5f, 0.5f);

        glBindTexture(GL_TEXTURE_2D, nullTexID);
        try (MemoryStack stack = stackPush()) {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 1, 1, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, stack.ints(0xFFFFFFFF));
        }
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        
        int BITMAP_W = 1024;
        int BITMAP_H = 1024;

        int FONT_HEIGHT = 18;
        int fontTexID   = glGenTextures();

        STBTTFontinfo          fontInfo = STBTTFontinfo.create();
        STBTTPackedchar.Buffer cdata    = STBTTPackedchar.create(95);

        float scale;
        float descent;

        try (MemoryStack stack = stackPush()) {
            stbtt_InitFont(fontInfo, ttf);
            scale = stbtt_ScaleForPixelHeight(fontInfo, FONT_HEIGHT);

            IntBuffer d = stack.mallocInt(1);
            stbtt_GetFontVMetrics(fontInfo, null, d, null);
            descent = d.get(0) * scale;

            ByteBuffer bitmap = memAlloc(BITMAP_W * BITMAP_H);

            STBTTPackContext pc = STBTTPackContext.mallocStack(stack);
            stbtt_PackBegin(pc, bitmap, BITMAP_W, BITMAP_H, 0, 1, NULL);
            stbtt_PackSetOversampling(pc, 4, 4);
            stbtt_PackFontRange(pc, ttf, 0, FONT_HEIGHT, 32, cdata);
            stbtt_PackEnd(pc);

            // Convert R8 to RGBA8
            ByteBuffer texture = memAlloc(BITMAP_W * BITMAP_H * 4);
            for (int i = 0; i < bitmap.capacity(); i++) {
                texture.putInt((bitmap.get(i) << 24) | 0x00FFFFFF);
            }
            texture.flip();

            glBindTexture(GL_TEXTURE_2D, fontTexID);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, BITMAP_W, BITMAP_H, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, texture);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

            memFree(texture);
            memFree(bitmap);
        }

        default_font
            .width((handle, h, text, len) -> {
                float text_width = 0;
                try (MemoryStack stack = stackPush()) {
                    IntBuffer unicode = stack.mallocInt(1);

                    int glyph_len = nnk_utf_decode(text, memAddress(unicode), len);
                    int text_len  = glyph_len;

                    if (glyph_len == 0) {
                        return 0;
                    }

                    IntBuffer advance = stack.mallocInt(1);
                    while (text_len <= len && glyph_len != 0) {
                        if (unicode.get(0) == NK_UTF_INVALID) {
                            break;
                        }

                        /* query currently drawn glyph information */
                        stbtt_GetCodepointHMetrics(fontInfo, unicode.get(0), advance, null);
                        text_width += advance.get(0) * scale;

                        /* offset next glyph */
                        glyph_len = nnk_utf_decode(text + text_len, memAddress(unicode), len - text_len);
                        text_len += glyph_len;
                    }
                }
                return text_width;
            })
            .height(FONT_HEIGHT)
            .query((handle, font_height, glyph, codepoint, next_codepoint) -> {
                try (MemoryStack stack = stackPush()) {
                    FloatBuffer x = stack.floats(0.0f);
                    FloatBuffer y = stack.floats(0.0f);

                    STBTTAlignedQuad q       = STBTTAlignedQuad.mallocStack(stack);
                    IntBuffer        advance = stack.mallocInt(1);

                    stbtt_GetPackedQuad(cdata, BITMAP_W, BITMAP_H, codepoint - 32, x, y, q, false);
                    stbtt_GetCodepointHMetrics(fontInfo, codepoint, advance, null);

                    NkUserFontGlyph ufg = NkUserFontGlyph.create(glyph);

                    ufg.width(q.x1() - q.x0());
                    ufg.height(q.y1() - q.y0());
                    ufg.offset().set(q.x0(), q.y0() + (FONT_HEIGHT + descent));
                    ufg.xadvance(advance.get(0) * scale);
                    ufg.uv(0).set(q.s0(), q.t0());
                    ufg.uv(1).set(q.s1(), q.t1());
                }
            })
            .texture(it -> it
                .id(fontTexID));

        nk_style_set_font(ctx, default_font);
    }
    
    public boolean getAA() {
    	return AA;
    }
    
    public void setAA(boolean AA) {
    	this.AA = AA;
    }
    
    private void setupShader() throws Exception {
        shader.createVertexShader(Utils.loadResource("/main/resources/shaders/hud_vertex.vs"));
        shader.createFragmentShader(Utils.loadResource("/main/resources/shaders/hud_fragment.fs"));
        shader.link();

        // Create uniforms for Ortographic-model projection matrix and base color
        shader.createUniform("projModelMatrix");
        shader.createUniform("texture_sampler");
        shader.createUniform("hasTexture");
        shader.createUniform("inGame");
    }
    
    private NkContext setupWindow(long win) {
        nk_init(ctx, ALLOCATOR, null);
        ctx.clip()
            .copy((handle, text, len) -> {
                if (len == 0) {
                    return;
                }

                try (MemoryStack stack = stackPush()) {
                    ByteBuffer str = stack.malloc(len + 1);
                    memCopy(text, memAddress(str), len);
                    str.put(len, (byte)0);

                    glfwSetClipboardString(win, str);
                }
            })
            .paste((handle, edit) -> {
                long text = nglfwGetClipboardString(win);
                if (text != NULL) {
                    nnk_textedit_paste(edit, text, nnk_strlen(text));
                }
            });
        return ctx;
    }
    
    public void setElements(IHudElement[] l) {
    	elements = l;
    }

	@Override
	public void input(Window window) {
		long win = window.getWindowHandle();

        nk_input_begin(ctx);
        windowInput(window);
        mouseInput(window.getMouseInput());

        NkMouse mouse = ctx.input().mouse();
        if (mouse.grab()) {
            glfwSetInputMode(win, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
        } else if (mouse.grabbed()) {
            float prevX = mouse.prev().x();
            float prevY = mouse.prev().y();
            glfwSetCursorPos(win, prevX, prevY);
            mouse.pos().x(prevX);
            mouse.pos().y(prevY);
        } else if (mouse.ungrab()) {
            glfwSetInputMode(win, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }

        nk_input_end(ctx);
	}
	
	private void mouseInput(MouseInput mouse) {
		Map<Integer, Integer> buttonMap = mouse.getButtonMap();
		Set<Integer> buttons = buttonMap.keySet();
		// Button Input
		for(int button : buttons) {
			int nkButton;
            switch (button) {
                case GLFW_MOUSE_BUTTON_RIGHT:
                    nkButton = NK_BUTTON_RIGHT;
                    break;
                case GLFW_MOUSE_BUTTON_MIDDLE:
                    nkButton = NK_BUTTON_MIDDLE;
                    break;
                default:
                    nkButton = NK_BUTTON_LEFT;
            }
            nk_input_button(ctx, nkButton, (int)mouse.getCurrentPos().x, (int)mouse.getCurrentPos().y, buttonMap.get(button) == GLFW_PRESS);
		}
		
		// Cursor Input
		nk_input_motion(ctx, (int)mouse.getCurrentPos().x, (int)mouse.getCurrentPos().y);
		
		// Scroll Input
		try (MemoryStack stack = stackPush()) {
            NkVec2 scroll = NkVec2.mallocStack(stack)
                .x((float)mouse.getXOffset())
                .y((float)mouse.getYOffset());
            nk_input_scroll(ctx, scroll);
        }
		mouse.clearButtonMap();
	}
	
	private void windowInput(Window win) {
		Map<Integer, Integer> keyMap = win.getKeyMap();
		Set<Integer> keys = keyMap.keySet();
		List<Integer> codeList = win.getCodepointList();
		// Character Input
		for(int codepoint : codeList) {
			nk_input_unicode(ctx, codepoint);
		}
		
		// Key Input
		for(int key : keys) {
			boolean press = keyMap.get(key) == GLFW_PRESS;
            switch (key) {
                case GLFW_KEY_DELETE:
                    nk_input_key(ctx, NK_KEY_DEL, press);
                    break;
                case GLFW_KEY_ENTER:
                    nk_input_key(ctx, NK_KEY_ENTER, press);
                    break;
                case GLFW_KEY_TAB:
                    nk_input_key(ctx, NK_KEY_TAB, press);
                    break;
                case GLFW_KEY_BACKSPACE:
                    nk_input_key(ctx, NK_KEY_BACKSPACE, press);
                    break;
                case GLFW_KEY_UP:
                    nk_input_key(ctx, NK_KEY_UP, press);
                    break;
                case GLFW_KEY_DOWN:
                    nk_input_key(ctx, NK_KEY_DOWN, press);
                    break;
                case GLFW_KEY_HOME:
                    nk_input_key(ctx, NK_KEY_TEXT_START, press);
                    nk_input_key(ctx, NK_KEY_SCROLL_START, press);
                    break;
                case GLFW_KEY_END:
                    nk_input_key(ctx, NK_KEY_TEXT_END, press);
                    nk_input_key(ctx, NK_KEY_SCROLL_END, press);
                    break;
                case GLFW_KEY_PAGE_DOWN:
                    nk_input_key(ctx, NK_KEY_SCROLL_DOWN, press);
                    break;
                case GLFW_KEY_PAGE_UP:
                    nk_input_key(ctx, NK_KEY_SCROLL_UP, press);
                    break;
                case GLFW_KEY_LEFT_SHIFT:
                	break;
                case GLFW_KEY_RIGHT_SHIFT:
                    nk_input_key(ctx, NK_KEY_SHIFT, press);
                    break;
                case GLFW_KEY_LEFT_CONTROL:
                	break;
                case GLFW_KEY_RIGHT_CONTROL:
                    if (press) {
                    	nk_input_key(ctx, NK_KEY_COPY, win.isKeyPressed(GLFW_KEY_C));
                        nk_input_key(ctx, NK_KEY_PASTE, win.isKeyPressed(GLFW_KEY_P));
                        nk_input_key(ctx, NK_KEY_CUT, win.isKeyPressed(GLFW_KEY_X));
                        nk_input_key(ctx, NK_KEY_TEXT_UNDO, win.isKeyPressed(GLFW_KEY_Z));
                        nk_input_key(ctx, NK_KEY_TEXT_REDO, win.isKeyPressed(GLFW_KEY_R));
                        nk_input_key(ctx, NK_KEY_TEXT_WORD_LEFT, win.isKeyPressed(GLFW_KEY_LEFT));
                        nk_input_key(ctx, NK_KEY_TEXT_WORD_RIGHT, win.isKeyPressed(GLFW_KEY_RIGHT));
                        nk_input_key(ctx, NK_KEY_TEXT_LINE_START, win.isKeyPressed(GLFW_KEY_B));
                        nk_input_key(ctx, NK_KEY_TEXT_LINE_END, win.isKeyPressed(GLFW_KEY_E));
                    } else {
                    	nk_input_key(ctx, NK_KEY_LEFT, win.isKeyPressed(GLFW_KEY_LEFT));
                        nk_input_key(ctx, NK_KEY_RIGHT, win.isKeyPressed(GLFW_KEY_RIGHT));
                        nk_input_key(ctx, NK_KEY_COPY, false);
                        nk_input_key(ctx, NK_KEY_PASTE, false);
                        nk_input_key(ctx, NK_KEY_CUT, false);
                        nk_input_key(ctx, NK_KEY_SHIFT, false);
                    }
                    break;
               default:
            	   break;
            }
		}
		win.clearKeyMap();
		win.clearCodepointList();
	}
	
	@Override
	public void render(Window window) {
		int aa = AA ? NK_ANTI_ALIASING_ON : NK_ANTI_ALIASING_OFF;
        render(window, aa, MAX_VERTEX_BUFFER, MAX_ELEMENT_BUFFER);
	}
	
	private void render(Window window, int AA, int max_vertex_buffer, int max_element_buffer) {
		for (IHudElement e : elements) {
        	e.layout(ctx);
        }
        
		shader.bind();
		int width, height;
        
        try (MemoryStack stack = stackPush()) {
            IntBuffer widthBuff  = stack.mallocInt(1);
            IntBuffer heightBuff = stack.mallocInt(1);

            glfwGetWindowSize(window.getWindowHandle(), widthBuff, heightBuff);
            width = widthBuff.get(0);
            height = heightBuff.get(0);
        }
        
        Matrix4f ortho = transformation.getOrtho2DProjectionMatrix(0, width, height, 0);
        shader.setUniform("projModelMatrix", ortho);
        
        shader.setUniform("texture_sampler", 0);
        
        shader.setUniform("hasTexture", 1);
        
        shader.setUniform("inGame", 0);
        
        //glBlendEquation(GL_FUNC_ADD);
        if (window.getOptions().cullFace) {
        	glDisable(GL_CULL_FACE);
        }
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_SCISSOR_TEST);
        glActiveTexture(GL_TEXTURE0);
        
        // convert from command queue into draw list and draw to screen

        // allocate vertex and element buffer
        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);

        glBufferData(GL_ARRAY_BUFFER, max_vertex_buffer, GL_STREAM_DRAW);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, max_element_buffer, GL_STREAM_DRAW);

        // load draw vertices & elements directly into vertex + element buffer
        ByteBuffer vertices = Objects.requireNonNull(glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY, max_vertex_buffer, null));
        ByteBuffer elements = Objects.requireNonNull(glMapBuffer(GL_ELEMENT_ARRAY_BUFFER, GL_WRITE_ONLY, max_element_buffer, null));
        try (MemoryStack stack = stackPush()) {
            // fill convert configuration
            NkConvertConfig config = NkConvertConfig.callocStack(stack)
                .vertex_layout(VERTEX_LAYOUT)
                .vertex_size(20)
                .vertex_alignment(4)
                .null_texture(null_texture)
                .circle_segment_count(22)
                .curve_segment_count(22)
                .arc_segment_count(22)
                .global_alpha(1.0f)
                .shape_AA(AA)
                .line_AA(AA);

            // setup buffers to load vertices and elements
            NkBuffer vbuf = NkBuffer.mallocStack(stack);
            NkBuffer ebuf = NkBuffer.mallocStack(stack);

            nk_buffer_init_fixed(vbuf, vertices/*, max_vertex_buffer*/);
            nk_buffer_init_fixed(ebuf, elements/*, max_element_buffer*/);
            nk_convert(ctx, cmds, vbuf, ebuf, config);
        }
        glUnmapBuffer(GL_ELEMENT_ARRAY_BUFFER);
        glUnmapBuffer(GL_ARRAY_BUFFER);

        // iterate over and execute each draw command
        float fb_scale_x = (float)window.getWidth() / (float)width;
        float fb_scale_y = (float)window.getHeight() / (float)height;

        long offset = NULL;
        for (NkDrawCommand cmd = nk__draw_begin(ctx, cmds); cmd != null; cmd = nk__draw_next(cmd, cmds, ctx)) {
            if (cmd.elem_count() == 0) {
                continue;
            }
            glBindTexture(GL_TEXTURE_2D, cmd.texture().id());
            glScissor(
                (int)(cmd.clip_rect().x() * fb_scale_x),
                (int)((height - (int)(cmd.clip_rect().y() + cmd.clip_rect().h())) * fb_scale_y),
                (int)(cmd.clip_rect().w() * fb_scale_x),
                (int)(cmd.clip_rect().h() * fb_scale_y)
            );
            glDrawElements(GL_TRIANGLES, cmd.elem_count(), GL_UNSIGNED_SHORT, offset);
            offset += cmd.elem_count() * 2;
        }
        nk_clear(ctx);
        
        shader.unbind();
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        window.restoreState();
	}
	
	private void destroy() {
        glDeleteTextures(default_font.texture().id());
        glDeleteTextures(null_texture.texture().id());
        glDeleteBuffers(vboId);
        glDeleteBuffers(eboId);
        nk_buffer_free(cmds);
    }

	@Override
	public void cleanup() {
		Objects.requireNonNull(ctx.clip().copy()).free();
        Objects.requireNonNull(ctx.clip().paste()).free();
        nk_free(ctx);
        destroy();
        Objects.requireNonNull(default_font.query()).free();
        Objects.requireNonNull(default_font.width()).free();

        calc.numberFilter.free();

        Objects.requireNonNull(ALLOCATOR.alloc()).free();
        Objects.requireNonNull(ALLOCATOR.mfree()).free();
	}
}