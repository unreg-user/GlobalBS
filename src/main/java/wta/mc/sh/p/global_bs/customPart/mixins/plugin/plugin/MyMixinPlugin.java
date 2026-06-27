package wta.mc.sh.p.global_bs.customPart.mixins.plugin.plugin;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.fabricmc.loader.api.ModContainer;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@SuppressWarnings("SameParameterValue")
public class MyMixinPlugin implements IMixinConfigPlugin {
	private static final String GLOBAL_MIXINS_CLASS_DESC_I = "wta/mc/sh/p/global_bs/customPart/DirectionCMathP";
	private static final boolean GLOBAL_MIXINS_CLASS_IS_INTERFACE = false;

	private static String BLOCK_DESC_I;
	private static String OBJECT_DESC_I;

	public static String mapCN(MappingResolver resolver, String className){
		return resolver.mapClassName("official", className.replace('/', '.')).replace('.', '/');
	}

	@Override
	public void onLoad(String mixinPackage) {
		var resolver = FabricLoader.getInstance().getMappingResolver();
		BLOCK_DESC_I = mapCN(resolver, "net/minecraft/world/level/block/Block");
		OBJECT_DESC_I = "java/lang/Object";

		String BLOCK_STATE_DESC_I = mapCN(resolver, "net/minecraft/world/level/block/state/BlockState");
		String BLOCK_POS_DESC_I = mapCN(resolver, "net/minecraft/core/BlockPos");
		String DIRECTION_DESC_I = mapCN(resolver, "net/minecraft/core/Direction");
		String AXIS_DESC_I = mapCN(resolver, "net/minecraft/core/Direction$Axis");


		ClassTinkerers.enumBuilder(
					DIRECTION_DESC_I,
					"I",
					"I",
					"I",
					"Ljava/lang/String;",
					getDesc(mapCN(resolver, "net/minecraft/core/Direction$AxisDirection")),
					getDesc(AXIS_DESC_I),
					getDesc(mapCN(resolver, "net/minecraft/core/Vec3i"))
			  )
			  .addEnum("R_DOWN", () -> new Object[]{0, 0, 0, "r_down", null, null, null})
			  .addEnum("R_UP", () -> new Object[]{0, 0, 0, "r_up", null, null, null})
			  .addEnum("R_NORTH", () -> new Object[]{0, 0, 0, "r_north", null, null, null})
			  .addEnum("R_SOUTH", () -> new Object[]{0, 0, 0, "r_south", null, null, null})
			  .addEnum("R_WEST", () -> new Object[]{0, 0, 0, "r_west", null, null, null})
			  .addEnum("R_EAST", () -> new Object[]{0, 0, 0, "r_east", null, null, null})
			  .build();

		var modPaths = new ArrayList<Path>();
		for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			modPaths.addAll(mod.getRootPaths());
		}

		var childToParent = new HashMap<String, String>();
		for (Path rootPath : modPaths) {
			try (Stream<Path> walk = Files.walk(rootPath)) {
				walk.filter(path -> {
						  String s = path.toString();
						  return s.endsWith(".class") && !s.contains("resources/assets/") && !s.contains("resources/data/") && !s.contains("resources/META-INF/");
					  })
					  .forEach(classPath -> {
						  try (InputStream stream = Files.newInputStream(classPath)) {
							  var reader = new ClassReader(stream);
							  var node = new ClassNode(Opcodes.ASM9);
							  reader.accept(node, ClassReader.SKIP_CODE);

							  String superName = node.superName != null ? node.superName : null;

							  if (superName != null) {
								  childToParent.put(node.name, superName);
							  }

						  } catch (Exception exception) {
							  System.err.println("[GlobalBS] Failed to read class: " + classPath + ", " + exception);
						  }
					  });
			} catch (Exception _) {
			}
		}

		var need = new HashSet<String>();
		for (var i : childToParent.keySet()) {
			if (recursiveTestHierarchy(i, childToParent)) {
				need.add(i);
			}
		}

		try {
			for (String blockAsmName : need) {
				String blockDotName = blockAsmName.replace('/', '.');
				ClassTinkerers.addTransformation(blockDotName, classNode -> {
					for (MethodNode method : classNode.methods) {
						var instructions = method.instructions;

						AbstractInsnNode[] insnArray = instructions.toArray();

						for (AbstractInsnNode insn : insnArray) {
							globalPatchWithAnotherArg(method, insn, instructions, true, DIRECTION_DESC_I, BLOCK_STATE_DESC_I, "getRotated");
							globalPatchWithAnotherArg(method, insn, instructions, false, BLOCK_POS_DESC_I, BLOCK_STATE_DESC_I, "getRotated");
							modifyInReturn(method, insn, instructions, DIRECTION_DESC_I, "unmarkRotated");
							modifyInMethodWhatReturnsInClass(insn, instructions, DIRECTION_DESC_I, BLOCK_DESC_I, "markRotated");
							globalRedirectNonstatic(insn, instructions, "ordinal", DIRECTION_DESC_I, true, "()I");
						}
					}
				});
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String getDesc(String descInternal) {
		return "L" + descInternal + ";";
	}

	private static void unaryPatch(AbstractInsnNode insn, InsnList instructions, String typeDescI, String methodName) {
		var returnPatch = new InsnList();
		var typeDesc = getDesc(typeDescI);

		returnPatch.add(new MethodInsnNode(
			  Opcodes.INVOKESTATIC,
			  GLOBAL_MIXINS_CLASS_DESC_I,
			  methodName,
			  "(" + typeDesc + ")" + typeDesc,
			  GLOBAL_MIXINS_CLASS_IS_INTERFACE
		));
		instructions.insertBefore(insn, returnPatch);
	}

	private static void patchWithAnotherArg(AbstractInsnNode insn, InsnList instructions, String sourceDescI, String needDescI, String methodName, int activeNeedSlot) {
		if (activeNeedSlot == -1) return;
		var sourceDesc = getDesc(sourceDescI);
		var needDesc = getDesc(needDescI);

		var patch = new InsnList();
		patch.add(new VarInsnNode(Opcodes.ALOAD, activeNeedSlot));
		patch.add(new MethodInsnNode(
			  Opcodes.INVOKESTATIC,
			  GLOBAL_MIXINS_CLASS_DESC_I,
			  methodName,
			  "(" + sourceDesc + needDesc + ")" + sourceDesc,
			  GLOBAL_MIXINS_CLASS_IS_INTERFACE
		));
		instructions.insert(insn, patch);
	}

	private static void readyPatch(AbstractInsnNode insn, InsnList instructions, String methodName, String methodDesc) {
		var patch = new InsnList();
		patch.add(new MethodInsnNode(
			  Opcodes.INVOKESTATIC,
			  GLOBAL_MIXINS_CLASS_DESC_I,
			  methodName,
			  methodDesc,
			  GLOBAL_MIXINS_CLASS_IS_INTERFACE
		));
		instructions.insert(insn, patch);
	}

	private static void modifyInReturn(MethodNode method, AbstractInsnNode insn, InsnList instructions, String returnDescI, String methodName) {
		var returnDesc = getDesc(returnDescI);

		if (insn.getOpcode() == Opcodes.ARETURN) {
			if (returnDesc.equals(Type.getReturnType(method.desc).getDescriptor())) {
				unaryPatch(insn, instructions, returnDescI, methodName);
			}
		}
	}

	private static void modifyInMethodWhatReturnsInClass(AbstractInsnNode insn, InsnList instructions, String returnDescI, String inClassDescI, String methodName) {
		var returnDesc = getDesc(returnDescI);

		if (insn instanceof MethodInsnNode methodInsn) {
			if (returnDesc.equals(Type.getReturnType(methodInsn.desc).getDescriptor()) && inClassDescI.equals(methodInsn.owner)) {
				unaryPatch(insn, instructions, returnDescI, methodName);
			}
		}
	}

	private static void globalPatchWithAnotherArg(MethodNode method, AbstractInsnNode insn, InsnList instructions, boolean replaceFromStaticField, String sourceTypeDescI, String needTypeDescI, String methodName) {
		var sourceTypeDesc = getDesc(sourceTypeDescI);
		var needTypeDesc = getDesc(needTypeDescI);

		boolean isTarget = false;

		if (insn instanceof VarInsnNode varInsn && varInsn.getOpcode() == Opcodes.ALOAD) {
			if (isVarSlotWithType(method, insn, varInsn.var, sourceTypeDesc)) {
				isTarget = true;
			}
		} else if (insn instanceof MethodInsnNode methodInsn) {
			if (
				  sourceTypeDesc.equals(Type.getReturnType(methodInsn.desc).getDescriptor()) ||
						(methodInsn.getOpcode() == Opcodes.INVOKESPECIAL
							  && sourceTypeDescI.equals(methodInsn.owner)
							  && "<init>".equals(methodInsn.name))) {
				isTarget = true;
			}
		} else if (replaceFromStaticField
			  && insn instanceof FieldInsnNode fieldInsn
			  && fieldInsn.getOpcode() == Opcodes.GETSTATIC
			  && Objects.equals(fieldInsn.desc, sourceTypeDesc)) {
			isTarget = true;
		}

		if (isTarget) {
			int activeBlockStateSlot = findActiveSlotWithType(method, insn, needTypeDescI);
			patchWithAnotherArg(insn, instructions, sourceTypeDescI, needTypeDescI, methodName, activeBlockStateSlot);
		}
	}

	private static void globalRedirectStatic(AbstractInsnNode insn, InsnList instructions, String methodName, @Nullable String ownerDescI, @Nullable String methodDesc) {
		if (insn instanceof MethodInsnNode methodInsn
			  && methodInsn.getOpcode() == Opcodes.INVOKESTATIC
			  && (ownerDescI == null || ownerDescI.equals(methodInsn.owner))
			  && (methodDesc == null || methodInsn.desc.equals(methodDesc))) {
			readyPatch(insn, instructions, methodName, methodInsn.desc);
			instructions.remove(insn);
		}
	}

	private static void globalRedirectNonstatic(AbstractInsnNode insn, InsnList instructions, String methodName, String ownerDescI, boolean checkOwner, @Nullable String sourceMethodDesc) {
		if (insn instanceof MethodInsnNode methodInsn
			  && methodInsn.getOpcode() != Opcodes.INVOKESTATIC
			  && (ownerDescI == null || ownerDescI.equals(methodInsn.owner))
			  && (!checkOwner || sourceMethodDesc == null || sourceMethodDesc.equals(methodInsn.desc))) {
			readyPatch(insn, instructions, methodName, "(" + getDesc(ownerDescI) + methodInsn.desc.substring(1));
			instructions.remove(insn);
		}
	}

	private static boolean isVarSlotWithType(MethodNode method, AbstractInsnNode currentInsn, int slot, String typeDesc) {
		int currentSlot = (method.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
		for (Type type : Type.getArgumentTypes(method.desc)) {
			if (currentSlot == slot) {
				return typeDesc.equals(type.getDescriptor());
			}
			currentSlot += type.getSize();
		}

		if (method.localVariables != null) {
			int indexCurrent = method.instructions.indexOf(currentInsn);
			for (LocalVariableNode local : method.localVariables) {
				if (local.index == slot && typeDesc.equals(local.desc)) {
					int indexStart = method.instructions.indexOf(local.start);
					int indexEnd = method.instructions.indexOf(local.end);

					if (indexCurrent >= indexStart && indexCurrent <= indexEnd) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@SuppressWarnings("SameParameterValue")
	private static int findActiveSlotWithType(MethodNode method, AbstractInsnNode currentInsn, String typeDescI) {
		var typeDesc = getDesc(typeDescI);

		int slot = (method.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
		for (Type type : Type.getArgumentTypes(method.desc)) {
			if (typeDesc.equals(type.getDescriptor())) return slot;
			slot += type.getSize();
		}

		if (method.localVariables != null) {
			int indexCurrent = method.instructions.indexOf(currentInsn);

			int earliestStartIndex = Integer.MAX_VALUE;
			int earliestIndex = -1;
			for (LocalVariableNode local : method.localVariables) {
				if (typeDesc.equals(local.desc)) {
					int indexStart = method.instructions.indexOf(local.start);
					int indexEnd = method.instructions.indexOf(local.end);

					if (indexCurrent >= indexStart && indexCurrent <= indexEnd) {
						if (indexStart < earliestStartIndex) {
							earliestStartIndex = indexStart;
							earliestIndex = local.index;
						}
					}
				}
			}

			if (earliestStartIndex < Integer.MAX_VALUE) {
				return earliestIndex;
			}
		}
		return -1;
	}

	private static boolean recursiveTestHierarchy(String key, Map<String, String> childToParent) {
		if (BLOCK_DESC_I.equals(key)) {
			return true;
		} else if (OBJECT_DESC_I.equals(key) || key == null) {
			return false;
		} else {
			return recursiveTestHierarchy(childToParent.get(key), childToParent);
		}
	}

	@Override
	public String getRefMapperConfig() {
		return "";
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

	}

	@Override
	public List<String> getMixins() {
		return List.of();
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}
}
