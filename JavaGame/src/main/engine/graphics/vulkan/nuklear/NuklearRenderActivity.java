package main.engine.graphics.vulkan.nuklear;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK11.*;
import static org.lwjgl.vulkan.VK12.*;

import static main.engine.utility.ResourcePaths.Shaders;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.joml.Matrix4f;
import org.lwjgl.nuklear.*;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.shaderc.Shaderc;
import org.lwjgl.vulkan.*;

import main.engine.EngineProperties;
import main.engine.MouseInput;
import main.engine.Window;
import main.engine.graphics.GraphConstants;
import main.engine.graphics.Transformation;
import main.engine.graphics.hud.NKHudElement;
import main.engine.graphics.vulkan.*;
import main.engine.scene.Scene;
import main.engine.utility.ResourcePaths;
import main.engine.utility.Utils;

public class NuklearRenderActivity {
	
	public static final int MAX_ELEMENTS = 10;
	public static final int BUFFER_INITIAL_SIZE = 4 * 1024;
    public static final int MAX_VERTEX_BUFFER  = 512 * 1024;
    public static final int MAX_INDICES_BUFFER = 128 * 1024;
    public static final int NULL_TEXTURE_ID = 0;
    public static final int FONT_TEXTURE_ID = 1;
	
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
    private STBTTFontinfo fontInfo;
    private STBTTPackedchar.Buffer cdata;
    private NkBuffer cmds;
    private NkContext ctx;
    private NkUserFont default_font;
    private DescriptorPool descriptorPool;
    private DescriptorSetLayout[] descriptorSetLayouts;
    private Device device;
    private NKHudElement[] elements;
    private VKTexture fontsTexture;
    private TextureSampler textureSampler;
    private VulkanBuffer[] indicesBuffers;
    private NkDrawNullTexture null_texture;
    private VKTexture nullTexture;
    private Pipeline pipeline;
    private ShaderProgram shaderProgram;
    private SwapChain swapChain;
    private ByteBuffer ttf;
    private TextureDescriptorSet nullDescriptorSet;
    private TextureDescriptorSet fontDescriptorSet;
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
        createDescriptorSets(swapChain);
        createPipeline(pipelineCache, vkRenderPass);
    }
    
    public NKHudElement[] getElements() {
    	return elements;
    }
    
    public void setElements(NKHudElement[] elements) {
    	this.elements = elements;
    }
    
    public boolean getAA() {
    	return AA;
    }
    
    public void setAA(boolean AA) {
    	this.AA = AA;
    }
    
    public void cleanup() {
        textureDescriptorSetLayout.cleanup();
        textureSampler.cleanup();
        descriptorPool.cleanup();
        fontsTexture.cleanup();
        Arrays.stream(vertexBuffers).filter(Objects::nonNull).forEach(VulkanBuffer::cleanup);
        Arrays.stream(indicesBuffers).filter(Objects::nonNull).forEach(VulkanBuffer::cleanup);
        pipeline.cleanup();
        shaderProgram.cleanup();
        null_texture.free();
        default_font.free();
        cmds.free();
        ctx.free();
    }
    
    public void cleanupElements() {
    	for (NKHudElement elem : elements) {
    		//elem.cleanup();
    	}
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
        descriptorTypeCounts.add(new DescriptorPool.DescriptorTypeCount(2, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER));
        descriptorPool = new DescriptorPool(device, descriptorTypeCounts);
    }

    private void createDescriptorSets(SwapChain swapChain) {
        textureDescriptorSetLayout = new DescriptorSetLayout.SamplerDescriptorSetLayout(device, 0, VK_SHADER_STAGE_FRAGMENT_BIT, 0);
        descriptorSetLayouts = new DescriptorSetLayout[]{
                textureDescriptorSetLayout,
        };
        textureSampler = new TextureSampler(device, 1);
        nullDescriptorSet = new TextureDescriptorSet(descriptorPool, textureDescriptorSetLayout, nullTexture,
                textureSampler, 0);
        fontDescriptorSet = new TextureDescriptorSet(descriptorPool, textureDescriptorSetLayout, fontsTexture,
                textureSampler, 0);
    }

    private void createPipeline(PipelineCache pipelineCache, long vkRenderPass) {
        Pipeline.PipeLineCreationInfo pipeLineCreationInfo = new Pipeline.PipeLineCreationInfo(vkRenderPass,
                shaderProgram, 1, false, true, GraphConstants.MAT4X4_SIZE_BYTES,
                new NuklearVertexBufferStructure(), descriptorSetLayouts);
        pipeline = new Pipeline(pipelineCache, pipeLineCreationInfo);
        pipeLineCreationInfo.cleanup();
    }

    private void createShaders() {
        EngineProperties engineProperties = EngineProperties.INSTANCE;
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
    	cmds = NkBuffer.create();
    	nk_buffer_init(cmds, ALLOCATOR, BUFFER_INITIAL_SIZE);
    	
    	createNullTexture(commandPool, queue);
    	createFontsTexture(ResourcePaths.Fonts.FIRASANS_REGULAR, commandPool, queue);
    	
    	vertexBuffers = new VulkanBuffer[swapChain.getNumImages()];
        indicesBuffers = new VulkanBuffer[swapChain.getNumImages()];
    }
    
    private void createNullTexture(CommandPool commandPool, Queue queue) {
    	try (MemoryStack stack = MemoryStack.stackPush()) {
    		nullTexture = new VKTexture(device, stack.bytes((byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF), 1, 1, VK_FORMAT_R8G8B8A8_SRGB);
    		
    		CommandBuffer cmd = new CommandBuffer(commandPool, true, true);
            cmd.beginRecording();
            nullTexture.recordTextureTransition(cmd);
            cmd.endRecording();
            cmd.submitAndWait(device, queue);
            cmd.cleanup();
    	}
    	
    	null_texture = NkDrawNullTexture.create();
    	null_texture.texture().id(NULL_TEXTURE_ID);
    	null_texture.uv().set(0.5f, 0.5f);
    }
    
    private void createFontsTexture(String filePath, CommandPool commandPool, Queue queue) {
    	int BITMAP_W = 1024;
        int BITMAP_H = 1024;
        int FONT_HEIGHT = 18;
        
        if (fontInfo == null) {
        	fontInfo = STBTTFontinfo.create();
        } else {
        	fontInfo.free();
        	fontInfo = STBTTFontinfo.create();
        }
        if (cdata == null) {
            cdata = STBTTPackedchar.create(95);
        } else {
        	cdata.free();
        	cdata = STBTTPackedchar.create(95);
        }
        
        float scale;
        float descent;
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
        	this.ttf = Utils.ioResourceToByteBuffer(filePath, 512 * 1024);
        	
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
            cmd.endRecording();
            cmd.submitAndWait(device, queue);
            cmd.cleanup();
            
            memFree(texture);
            memFree(bitmap);
        } catch (IOException e) {
        	throw new RuntimeException(e);
        }
        
        default_font = NkUserFont.create();
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
        .texture().id(FONT_TEXTURE_ID);
    	
    	nk_style_set_font(ctx, default_font);
    }
    
    private void layoutElements() {
    	for (NKHudElement element : elements) {
    		element.layout(ctx);
    	}
    }
    
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
		for (int button : buttons) {
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
	
	public void recordCommandBuffer(CommandBuffer commandBuffer) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			int idx = swapChain.getCurrentFrame();
			
			layoutElements();
			updateBuffers(idx);
            if (vertexBuffers[idx] == null) {
                return;
            }
            
            VkExtent2D swapChainExtent = swapChain.getSwapChainExtent();
            int width = swapChainExtent.width();
            int height = swapChainExtent.height();

            VkCommandBuffer cmdHandle = commandBuffer.getVkCommandBuffer();

            vkCmdBindPipeline(cmdHandle, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.getVkPipeline());

            VkViewport.Buffer viewport = VkViewport.calloc(1, stack)
                    .x(0)
                    .y(height)
                    .height(-height)
                    .width(width)
                    .minDepth(0.0f)
                    .maxDepth(1.0f);
            vkCmdSetViewport(cmdHandle, 0, viewport);
            
            LongBuffer vtxBuffer = stack.mallocLong(1);
            vtxBuffer.put(0, vertexBuffers[idx].getBuffer());
            LongBuffer offsets = stack.mallocLong(1);
            offsets.put(0, 0L);
            vkCmdBindVertexBuffers(cmdHandle, 0, vtxBuffer, offsets);
            vkCmdBindIndexBuffer(cmdHandle, indicesBuffers[idx].getBuffer(), 0, VK_INDEX_TYPE_UINT16);
            
            Matrix4f ortho = transformation.getOrtho2DProjectionMatrix(0, width, height, 0);
            VulkanUtils.setMatrixAsPushConstant(pipeline, cmdHandle, ortho);
            
            LongBuffer nullDescriptorSets = stack.mallocLong(1)
                    .put(0, this.nullDescriptorSet.getVkDescriptorSet());
            LongBuffer fontDescriptorSets = stack.mallocLong(1)
                    .put(0, this.fontDescriptorSet.getVkDescriptorSet());
            
            int offsetIdx = 0;
            VkRect2D.Buffer rect = VkRect2D.calloc(1, stack);
            for (NkDrawCommand cmd = nk__draw_begin(ctx, cmds); cmd != null; cmd = nk__draw_next(cmd, cmds, ctx)) {
            	if (cmd.elem_count() == 0) {
                    continue;
                }
            	int id = cmd.texture().id();
            	switch (id) {
            		case FONT_TEXTURE_ID: 
            			vkCmdBindDescriptorSets(cmdHandle, VK_PIPELINE_BIND_POINT_GRAPHICS,
                                pipeline.getVkPipelineLayout(), 0, fontDescriptorSets, null);
            			break;
            		default:
            			vkCmdBindDescriptorSets(cmdHandle, VK_PIPELINE_BIND_POINT_GRAPHICS,
                                pipeline.getVkPipelineLayout(), 0, nullDescriptorSets, null);
            			break;
            	}
            	int offsetX = (int) Math.max(cmd.clip_rect().x(), 0);
            	int offsetY = (int) Math.max(cmd.clip_rect().y(), 1);
            	rect.offset(it -> it.x(offsetX).y(offsetY));
            	int extentX = (int) cmd.clip_rect().w();
            	int extentY = (int) cmd.clip_rect().h();
            	rect.extent(it -> it.width(extentX).height(extentY));
            	vkCmdSetScissor(cmdHandle, 0, rect);
            	int elemCount = cmd.elem_count();
            	vkCmdDrawIndexed(cmdHandle, elemCount, 1, offsetIdx, 0, 0);
            	offsetIdx += elemCount;
            }
            nk_clear(ctx);
		}
	}
	
	public void resize(SwapChain swapChain) {
        this.swapChain = swapChain;
    }
	
	private void updateBuffers(int idx) {
        VulkanBuffer vertexBuffer = vertexBuffers[idx];
        if (vertexBuffer == null || MAX_VERTEX_BUFFER != vertexBuffer.getRequestedSize()) {
            if (vertexBuffer != null) {
                vertexBuffer.cleanup();
            }
            vertexBuffer = new VulkanBuffer(device, MAX_VERTEX_BUFFER, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, 0);
            vertexBuffers[idx] = vertexBuffer;
        }

        VulkanBuffer indicesBuffer = indicesBuffers[idx];
        if (indicesBuffer == null || MAX_INDICES_BUFFER != indicesBuffer.getRequestedSize()) {
            if (indicesBuffer != null) {
                indicesBuffer.cleanup();
            }
            indicesBuffer = new VulkanBuffer(device, MAX_INDICES_BUFFER, VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, 0);
            indicesBuffers[idx] = indicesBuffer;
        }

        ByteBuffer dstVertexBuffer = MemoryUtil.memByteBuffer(vertexBuffer.map(), MAX_VERTEX_BUFFER);
        ByteBuffer dstIdxBuffer = MemoryUtil.memByteBuffer(indicesBuffer.map(), MAX_INDICES_BUFFER);

        try (MemoryStack stack = stackPush()) {
            // fill convert configuration
            NkConvertConfig config = NkConvertConfig.calloc(stack)
                .vertex_layout(VERTEX_LAYOUT)
                .vertex_size(20)
                .vertex_alignment(4)
                .null_texture(null_texture)
                .circle_segment_count(22)
                .curve_segment_count(22)
                .arc_segment_count(22)
                .global_alpha(1.0f)
                .shape_AA(AA ? NK_ANTI_ALIASING_ON : NK_ANTI_ALIASING_OFF)
                .line_AA(AA ? NK_ANTI_ALIASING_ON : NK_ANTI_ALIASING_OFF);

            // setup buffers to load vertices and elements
            NkBuffer vbuf = NkBuffer.malloc(stack);
            NkBuffer ebuf = NkBuffer.malloc(stack);

            nk_buffer_init_fixed(vbuf, dstVertexBuffer/*, max_vertex_buffer*/);
            nk_buffer_init_fixed(ebuf, dstIdxBuffer/*, max_element_buffer*/);
            nk_convert(ctx, cmds, vbuf, ebuf, config);
        }

        vertexBuffer.flush();
        indicesBuffer.flush();

        vertexBuffer.unMap();
        indicesBuffer.unMap();
    }
}
