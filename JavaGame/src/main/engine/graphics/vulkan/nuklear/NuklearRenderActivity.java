package main.engine.graphics.vulkan.nuklear;

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
import static org.lwjgl.glfw.GLFW.glfwSetClipboardString;
import static org.lwjgl.glfw.GLFW.nglfwGetClipboardString;
import static org.lwjgl.nuklear.Nuklear.*;
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
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK11.*;

import static main.engine.utility.ResourcePaths.Shaders;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.lwjgl.nuklear.NkAllocator;
import org.lwjgl.nuklear.NkBuffer;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkDrawNullTexture;
import org.lwjgl.nuklear.NkDrawVertexLayoutElement;
import org.lwjgl.nuklear.NkUserFont;
import org.lwjgl.nuklear.NkUserFontGlyph;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.shaderc.Shaderc;

import main.engine.EngineProperties;
import main.engine.MouseInput;
import main.engine.Scene;
import main.engine.Window;
import main.engine.graphics.GraphConstants;
import main.engine.graphics.Transformation;
import main.engine.graphics.hud.NKHudElement;
import main.engine.graphics.vulkan.*;
import main.engine.utility.Utils;

public class NuklearRenderActivity {
	
	public static final int MAX_ELEMENTS = 10;
	public static final int BUFFER_INITIAL_SIZE = 4 * 1024;
    public static final int MAX_VERTEX_BUFFER  = 512 * 1024;
    public static final int MAX_ELEMENT_BUFFER = 128 * 1024;
	
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
    
    private final Transformation transformation;
    
    private boolean AA;
    private NkBuffer cmds;
    private NkContext ctx;
    private NkUserFont default_font;
    private DescriptorPool descriptorPool;
    private DescriptorSetLayout[] descriptorSetLayouts;
    private Device device;
    private NKHudElement[] elements;
    private VKTexture fontsTexture;
    private TextureSampler fontsTextureSampler;
    private VulkanBuffer[] indicesBuffers;
    private int elementIdx;
    private NkDrawNullTexture null_texture;
    private VKTexture nullTexture;
    private Pipeline pipeline;
    private ShaderProgram shaderProgram;
    private SwapChain swapChain;
    private ByteBuffer ttf;
    private TextureDescriptorSet textureDescriptorSet;
    private DescriptorSetLayout.SamplerDescriptorSetLayout textureDescriptorSetLayout;
    private VulkanBuffer[] vertexBuffers;
    
    public NuklearRenderActivity(SwapChain swapChain, CommandPool commandPool, Queue queue, PipelineCache pipelineCache,
            long vkRenderPass, Window window) {
    	this.swapChain = swapChain;
        device = swapChain.getDevice();
        transformation = new Transformation();
        elements = new NKHudElement[MAX_ELEMENTS];
    	AA = true;

        createContext(window);
        createShaders();
        createUIResources(swapChain, commandPool, queue);
        createDescriptorPool();
        createDescriptorSets();
        createPipeline(pipelineCache, vkRenderPass);
    }
    
    public int addElement(NKHudElement element) {
    	elements[elementIdx] = element;
    	return elementIdx++;
    }
    
    public void removeElement(int index) {
    	elements[index] = null;
    }
    
    public boolean getAA() {
    	return AA;
    }
    
    public void setAA(boolean AA) {
    	this.AA = AA;
    }
    
    public void cleanup() {
        textureDescriptorSetLayout.cleanup();
        fontsTextureSampler.cleanup();
        descriptorPool.cleanup();
        fontsTexture.cleanup();
        Arrays.stream(vertexBuffers).filter(Objects::nonNull).forEach(VulkanBuffer::cleanup);
        Arrays.stream(indicesBuffers).filter(Objects::nonNull).forEach(VulkanBuffer::cleanup);
        pipeline.cleanup();
        shaderProgram.cleanup();
    }
    
    private void createContext(Window window) {
    	ctx = NkContext.create();
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

                    glfwSetClipboardString(window.getWindowHandle(), str);
                }
            })
            .paste((handle, edit) -> {
                long text = nglfwGetClipboardString(window.getWindowHandle());
                if (text != NULL) {
                    nnk_textedit_paste(edit, text, nnk_strlen(text));
                }
            });
    }

    private void createDescriptorPool() {
        List<DescriptorPool.DescriptorTypeCount> descriptorTypeCounts = new ArrayList<>();
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(1, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER));
        descriptorPool = new DescriptorPool(device, descriptorTypeCounts);
    }

    private void createDescriptorSets() {
        textureDescriptorSetLayout = new DescriptorSetLayout.SamplerDescriptorSetLayout(device, 0, VK_SHADER_STAGE_FRAGMENT_BIT);
        descriptorSetLayouts = new DescriptorSetLayout[]{
                textureDescriptorSetLayout,
        };
        fontsTextureSampler = new TextureSampler(device, 1);
        textureDescriptorSet = new TextureDescriptorSet(descriptorPool, textureDescriptorSetLayout, fontsTexture,
                fontsTextureSampler, 0);
    }

    private void createPipeline(PipelineCache pipelineCache, long vkRenderPass) {
        Pipeline.PipeLineCreationInfo pipeLineCreationInfo = new Pipeline.PipeLineCreationInfo(vkRenderPass,
                shaderProgram, 1, false, true, GraphConstants.FLOAT_SIZE_BYTES * 2,
                new NuklearVertexBufferStructure(), descriptorSetLayouts);
        pipeline = new Pipeline(pipelineCache, pipeLineCreationInfo);
        pipeLineCreationInfo.cleanup();
    }

    private void createShaders() {
        EngineProperties engineProperties = EngineProperties.getInstance();
        if (engineProperties.isShaderRecompilation()) {
            ShaderCompiler.compileShaderIfChanged(Shaders.Vulkan.NUKLEAR_VERTEX_GLSL, Shaderc.shaderc_glsl_vertex_shader);
            ShaderCompiler.compileShaderIfChanged(Shaders.Vulkan.NUKLEAR_FRAGMENT_GLSL, Shaderc.shaderc_glsl_fragment_shader);
        }
        shaderProgram = new ShaderProgram(device, new ShaderProgram.ShaderModuleData[]
                {
                        new ShaderProgram.ShaderModuleData(VK_SHADER_STAGE_VERTEX_BIT, Shaders.Vulkan.NUKLEAR_VERTEX_SPV),
                        new ShaderProgram.ShaderModuleData(VK_SHADER_STAGE_FRAGMENT_BIT, Shaders.Vulkan.NUKLEAR_FRAGMENT_SPV),
                });
    }
    
    private void createUIResources(SwapChain swapChain, CommandPool commandPool, Queue queue) {
    	try {
            this.ttf = Utils.ioResourceToByteBuffer(System.getProperty("user.dir") + "\\src\\main\\resources\\fonts\\FiraSans-Regular.ttf", 512 * 1024);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    	
    	nk_buffer_init(cmds, ALLOCATOR, BUFFER_INITIAL_SIZE);
    	
    	int BITMAP_W = 1024;
        int BITMAP_H = 1024;
        int FONT_HEIGHT = 18;
        
        STBTTFontinfo fontInfo = STBTTFontinfo.create();
        STBTTPackedchar.Buffer cdata = STBTTPackedchar.create(95);
        
        float scale;
        float descent;
    	
    	try (MemoryStack stack = MemoryStack.stackPush()) {
    		nullTexture = new VKTexture(device, stack.bytes((byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF), 1, 1, VK_FORMAT_R8G8B8A8_SRGB);
            
            stbtt_InitFont(fontInfo, ttf);
            scale = stbtt_ScaleForPixelHeight(fontInfo, FONT_HEIGHT);
            
            IntBuffer d = stack.mallocInt(1);
            stbtt_GetFontVMetrics(fontInfo, null, d, null);
            descent = d.get(0) * scale;
            
            ByteBuffer bitmap = MemoryUtil.memAlloc(BITMAP_W * BITMAP_H);
            
            STBTTPackContext pc = STBTTPackContext.malloc(stack);
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
            
            fontsTexture = new VKTexture(device, texture, BITMAP_W, BITMAP_H, VK_FORMAT_R8G8B8A8_SRGB);
            
            CommandBuffer cmd = new CommandBuffer(commandPool, true, true);
            cmd.beginRecording();
            fontsTexture.recordTextureTransition(cmd);
            nullTexture.recordTextureTransition(cmd);
            cmd.endRecording();
            cmd.submitAndWait(device, queue);
            cmd.cleanup();
            
            memFree(texture);
            memFree(bitmap);
    	}
    	
    	null_texture.texture().ptr(nullTexture.getImageView().getVkImageView());
    	null_texture.uv().set(0.5f, 0.5f);
    	
    	default_font
        .width((handle, h, text, len) -> {
            float text_width = 0;
            try (MemoryStack stack = stackPush()) {
                IntBuffer unicode = stack.mallocInt(1);

                int glyph_len = nnk_utf_decode(text, memAddress(unicode), len);
                int text_len = glyph_len;

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

                STBTTAlignedQuad q = STBTTAlignedQuad.malloc(stack);
                IntBuffer advance = stack.mallocInt(1);

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
        .texture(t -> t.ptr(fontsTexture.getImageView().getVkImageView()));
    	
    	nk_style_set_font(ctx, default_font);
    	
    	fontInfo.free();
    	cdata.free();
    	
    	vertexBuffers = new VulkanBuffer[swapChain.getNumImages()];
        indicesBuffers = new VulkanBuffer[swapChain.getNumImages()];
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
            NkVec2 scroll = NkVec2.malloc(stack)
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
	
	public void recordCommandBuffer(Scene scene, CommandBuffer commandBuffer) {
	}
}
