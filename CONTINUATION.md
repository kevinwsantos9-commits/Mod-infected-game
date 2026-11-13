# CONTINUATION — manual para outra IA

## Objetivo do mod
NIVEK: ECLIPSE (Fabric 1.21.8). Um meteoro cai perto do jogador. Ele toca o núcleo extraterrestre e se funde com ele (tentáculos, mutação nível 1). Depois evolui em 5 estágios com o ECLIPSE SYSTEM (interface), braço biomecânico, lâmina/garras, implantes, criaturas e chefes, e transformações finais (tentáculos, asas, Armadura Eclipse).

## História
Meteoro -> cratera -> núcleo alienígena (evento único por jogador) -> fusão cinematográfica -> mutação nível 1 -> evolução.

## Arquitetura
- `src/main`: lógica comum (servidor + cliente). Entrypoint `NivekEclipse`.
- `src/client`: render, GUI, modelos, input. Entrypoint `client.NivekEclipseClient`.
- Mod id `nivekeclipse`. Pacote `com.nivek.eclipse`.

## Estrutura atual
```
build.gradle, settings.gradle, gradle.properties, .github/workflows/build.yml
src/main/java/com/nivek/eclipse/
  NivekEclipse.java            (entrypoint; chama init dos sistemas)
  registry/ModBlocks.java      (bloco + item alien_core, aba criativa)
  data/ModAttachments.java     (METEOR_EVENT_DONE, MUTATION_LEVEL, ENERGY; persistentes; MUTATION_LEVEL sincroniza com todos os clientes, ENERGY só com o dono)
  data/EclipseData.java        (get/setMutationLevel)
  block/AlienCoreBlock.java    (toque chama FusionEvent.tryStart)
  event/FusionEvent.java       (sequência de fusão de 100 ticks; concede mutação 1 no fim)
  event/MeteorEvent.java       (gatilho, queda, cratera, núcleo; start() público)
  combat/AbilityPayload.java   (pacote C2S: id da habilidade)
  combat/CooldownPayload.java  (pacote S2C: recarga para o HUD)
  combat/CombatManager.java    (energia, cooldown, combos, Lâmina e Garras; registro dos pacotes)
  command/EclipseCommand.java  (/eclipse meteor | status | mutation <0-5> | reset)
src/client/java/com/nivek/eclipse/client/
  NivekEclipseClient.java      (G -> EclipseScreen; V/R -> AbilityPayload; layer do braço; HUD; receiver de recarga)
  ClientCombatState.java       (recarga como o HUD enxerga)
  hud/EclipseHud.java          (energia + recarga, canto inferior direito)
  gui/EclipseScreen.java       (painel, sidebar, clique nas abas, aba OVERVIEW)
  gui/EclipseTab.java          (enum das 11 abas + nota da fase)
  render/BioArmModel.java      (LayerDefinition + textura do braço)
  render/BioArmLayer.java      (RenderLayer no PlayerRenderer; idle/transformação/ataque)
  render/EclipseRenderState.java (interface implementada no PlayerRenderState via mixin)
  render/ArmTransformTracker.java (detecta mutação 0 -> 1 e calcula progresso)
  mixin/PlayerRenderStateMixin.java, mixin/PlayerRendererMixin.java
src/client/resources/nivekeclipse.client.mixins.json
src/main/resources/assets/nivekeclipse/textures/entity/bio_arm.png
src/main/resources/fabric.mod.json
src/main/resources/assets/nivekeclipse/ (blockstates, models/block, items, textures/block, lang en_us/pt_br)
```

## Sistemas implementados
- Fase 01: fundação.
- Fase 02: evento do meteoro (ver DEVELOPMENT_STATUS). Meteoro simulado em `MeteorEvent` (lista ACTIVE, tick no END_SERVER_TICK), sem entidade.
- Fase 03: fusão em `FusionEvent` (mapa ACTIVE por UUID, tick no END_SERVER_TICK). Núcleo e mutação só mudam no fim; cancelamento não perde nada.
- Fase 04 (etapa 4.1): ECLIPSE SYSTEM em `client/gui`. Tela própria sem widgets (desenha com GuiGraphics e trata clique em `mouseClicked`). Para adicionar conteúdo a uma aba, trate-a em `EclipseScreen.render` (como `renderOverview`) e remova o placeholder.
- Fase 05 (etapa 5.1): braço biomecânico como `RenderLayer` (registrado em `NivekEclipseClient` via `LivingEntityFeatureRendererRegistrationCallback`). O layer não conhece a entidade, só o `PlayerRenderState`; por isso os dados (mutação, progresso da transformação) são colocados no state por mixin (`PlayerRendererMixin`). Para novas peças, adicione partes em `BioArmModel` e anime em `BioArmLayer`.
- Fase 06 (etapa 6.1): o cliente só PEDE a habilidade (`AbilityPayload`); o servidor valida mutação, energia e cooldown em `CombatManager`. Para uma nova habilidade: novo id em `AbilityPayload` (e tamanho dos arrays de recarga: 3 posições, índice = id), `AbilityDef` + ramo em `CombatManager.handle`, tecla em `NivekEclipseClient`, linha no `EclipseHud`. Combos: janela de 40 ticks, golpe final no 3º (estado em `COMBO`).

## APIs e dependências
Fabric Loader 0.19.5, Fabric API 0.136.1+1.21.8 (Attachment API, ItemGroupEvents, ServerTickEvents, CommandRegistrationCallback v2), Loom 1.11-SNAPSHOT, mappings oficiais Mojang, Java 21.

## Convenções
- 1.21.8: cores do GuiGraphics sempre com alfa 0xFF; `mouseClicked(double,double,int)` (muda em 1.21.9+); KeyMapping com categoria String.
- 1.21.8: `PlayerRenderer` fica em `...entity.player` (vira `AvatarRenderer` na 1.21.9); `RenderLayer.render(PoseStack, MultiBufferSource, int, State, float, float)` (muda para `submit` na 1.21.9).
- 1.21.8: dano no servidor com `entity.hurtServer(ServerLevel, DamageSource, float)` (não `hurt`); rede com `PayloadTypeRegistry.playC2S()` + `ServerPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> ...)`.
- Mappings da Mojang: nomes de classes vêm deles (ex.: `ResourceLocation`, `ServerPlayer`), não Yarn.
- Uma classe de registro por tipo (itens, blocos, entidades...), sem duplicar registradores.
- Constantes em `NivekEclipse` (`MOD_ID`, `LOGGER`).
- Não chutar APIs: conferir na versão 1.21.8.

## Problemas e soluções
- O wrapper do Gradle não está no projeto; o CI instala o Gradle 8.14.3 direto. Localmente use Gradle 8.14+ instalado.

## Última fase concluída
Fase 01 (build confirmado). Fases 02, 03, 04.1, 05.1, 06.1 e 06.2 escritas; build pendente (usuário mandou seguir sem confirmar).

## Próxima tarefa exata
1. Rodar o build e corrigir erros das Fases 02 a 06.2. Riscos principais: `hurtServer`, `Entity.hurtMarked/push`, `HudRenderCallback`, callback de render, mixins, `syncWith`, `ServerPlayNetworking`/`ClientPlayNetworking`.
2. Testar: `/eclipse mutation 1`; V e R perto de mobs; encadear 3 golpes em 2 s (combo final); HUD; energia/recarga.
3. Atualizar DEVELOPMENT_STATUS.md com o resultado real.
4. Então: Fase 07 (implantes: cabeça, braços, costas, pernas, peitoral).

## Como continuar
Ler README, DEVELOPMENT_STATUS e CONTINUATION; não recriar o projeto, não apagar sistemas existentes, não alterar versões sem necessidade.
