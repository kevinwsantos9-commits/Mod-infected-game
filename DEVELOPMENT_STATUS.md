# DEVELOPMENT_STATUS

- Projeto: NIVEK: ECLIPSE, versão 0.1.0
- Mod id: `nivekeclipse` | Pacote: `com.nivek.eclipse`
- Minecraft: 1.21.8 | Fabric Loader: 0.19.5 | Fabric API: 0.136.1+1.21.8
- Loom: 1.11-SNAPSHOT (Gradle 8.14.3 no CI) | Mappings: oficiais Mojang | Java: 21

## Fases concluídas
- FASE 01 — Fundação (build passou no GitHub Actions, confirmado pelo usuário)

## Fase atual
FASE 06 — Combate, etapa 6.2 escrita (aguardando build). As Fases 02, 03, 04.1, 05.1 e 06.1 também NÃO tiveram o build confirmado (usuário mandou seguir).

## Fases pendentes
04 ECLIPSE SYSTEM, 04 (conteúdo das abas), 05 (refinamento do braço, ver abaixo), 06 (ataque carregado, opcional), 07 Implantes, 08 Evolução, 09 Criaturas e chefes, 10 Transformações, 11 Finalização

## Funcionalidades implementadas
- Fase 01: projeto Gradle/Loom, entrypoints, workflow
- Fase 02 (não verificada): bloco/item Núcleo Alienígena (textura gerada, lang en/pt_br), dado por jogador `meteor_event_done` (Attachment persistente), gatilho (noite, céu aberto, Overworld, ~1/30 a cada 10 s), queda simulada no servidor com partículas e sons, cratera (raio 7, profundidade 4), núcleo no fundo, comandos `/eclipse meteor` e `/eclipse reset` (op 2)

- Fase 03 (não verificada): ao tocar o núcleo, sequência de 5 s (jogador preso no lugar, 6 tentáculos de partículas, sons, partículas de portal); no fim concede mutação nível 1 (Attachment `mutation_level`, persistente, copyOnDeath) e consome o núcleo; cancela sem perdas se morrer/desconectar; `/eclipse status` e `/eclipse reset` (agora zera também a mutação)

- Fase 04 etapa 4.1 (não verificada): `mutation_level` sincroniza com o cliente (syncWith VAR_INT, targetOnly); tecla G abre o ECLIPSE SYSTEM (só com mutação >= 1); tela com 11 abas e navegação lateral por clique; aba OVERVIEW funcional (jogador, estágio, mutação x/5, barra); demais abas são placeholders; `/eclipse mutation <0-5>` para teste

- Fase 05 etapa 5.1 (não verificada): braço biomecânico (ombro, antebraço, garras) desenhado como RenderLayer sobre o braço direito de qualquer jogador com mutação >= 1 (mutação agora sincroniza com todos os clientes); textura 64x32 gerada por script; animações: idle (pulso + dedos), transformação (cresce em 40 ticks quando a mutação passa de 0 para >=1), ataque (garras abrem com `attackTime`); 2 mixins client (PlayerRenderState ganha dados extras; PlayerRenderer.extractRenderState preenche)

- Fase 06 etapa 6.1 (não verificada): energia Eclipse 0-100 (persistente, sync só com o dono, regen +2 a cada 10 ticks com mutação >= 1), pacote cliente->servidor `AbilityPayload`, `CombatManager` (valida mutação, cooldown 30 ticks, custo 20); Lâmina Biológica (tecla V): golpe em cone de 3,5 blocos, 8 de dano, partículas (varredura + arco de chamas de alma) e som; barra de energia na aba OVERVIEW

- Fase 06 etapa 6.2 (não verificada): Garras (tecla R: custo 8, recarga 10 ticks, 4 de dano, alcance 2,8, riscos de partículas CRIT); combos (janela de 40 ticks, 3 golpes: x1.0, x1.25, x1.5 + golpe final com repulsão e explosão visual); `CooldownPayload` servidor->cliente; HUD no canto inferior direito (barra de energia + recarga de V e R); `AbilityDef` em `CombatManager`

## Bugs conhecidos / limitações
- Fase 06.2: ataque carregado NÃO implementado; HUD usa `HudRenderCallback` (depreciado em versões novas do Fabric API); combo e golpes podem ferir aliados/jogadores; garras sem modelo/animação 3D própria; sem balanceamento
- Fase 05: modelo simples (caixas), pensado para braço clássico (4px; skin slim pode desalinhar 1px); só o braço direito; textura procedural provisória; sem emissividade; animações simples. Mixin com defaultRequire=1: se o descriptor estiver errado, o jogo falha ao abrir com erro claro. `ArmTransformTracker` não limpa entradas de quem sai (vazamento pequeno)
- Fase 04: abas além de OVERVIEW só mostram "Disponivel em breve" (o conteúdo depende dos sistemas das Fases 05–10); sem animações/ícones/texturas de UI; sem toggle da tecla G dentro da tela (ESC fecha)
- Fusão: sem invulnerabilidade durante a sequência; qualquer jogador sem mutação pode usar o núcleo
- Fusão: tentáculos são só partículas (sem modelo); sem câmera cinematográfica (exige código de cliente)
- Meteoro é só partículas (sem entidade/modelo): visível apenas a ~32 blocos
- Sem tremor de tela (exige pacote de rede)
- Núcleo só mostra mensagem ao toque; fusão é a Fase 03
- Núcleo é indestrutível em survival (strength -1)

## Último resultado da compilação
Fases 02, 03, 04.1, 05.1, 06.1 e 06.2: AINDA NÃO TESTADAS.
