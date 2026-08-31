# Graph Report - prev_fraude_bancario  (2026-08-31)

## Corpus Check
- 29 files · ~5,075 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 93 nodes · 103 edges · 13 communities (8 shown, 5 thin omitted)
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `641eeb05`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]

## God Nodes (most connected - your core abstractions)
1. `FraudMockService` - 7 edges
2. `Frontend` - 7 edges
3. `Instrucciones IA - Orquestador: Sistema de Detección de Fraude Bancario` - 6 edges
4. `Instrucciones IA - Backend: Sistema de Detección de Fraude Bancario` - 6 edges
5. `Instrucciones IA - Frontend: Sistema de Detección de Fraude Bancario` - 6 edges
6. `AlertsListComponent` - 5 edges
7. `DashboardComponent` - 5 edges
8. `Getting Started` - 4 edges
9. `AppComponent` - 3 edges
10. `FraudAlert` - 3 edges

## Surprising Connections (you probably didn't know these)
- None detected - all connections are within the same source files.

## Communities (13 total, 5 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.16
Nodes (5): AlertsListComponent, DashboardComponent, DashboardMetrics, FraudAlert, FraudMockService

### Community 1 - "Community 1"
Cohesion: 0.14
Nodes (13): Additional Resources, Building, Code scaffolding, code:bash (ng serve), code:bash (ng generate component component-name), code:bash (ng generate --help), code:bash (ng build), code:bash (ng test) (+5 more)

### Community 2 - "Community 2"
Cohesion: 0.33
Nodes (3): MainLayoutComponent, NavbarComponent, SidebarComponent

### Community 3 - "Community 3"
Cohesion: 0.31
Nodes (5): AppComponent, compiled, fixture, appConfig, routes

### Community 4 - "Community 4"
Cohesion: 0.22
Nodes (8): 1. Stack Tecnológico, 2. Arquitectura de Software: Clean Architecture / Hexagonal, 3. Patrones de Diseño a Implementar, 4. Reglas Críticas para la Implementación (IA), code:block1 (com.empresa.fraude), code:json ({), Instrucciones IA - Backend: Sistema de Detección de Fraude Bancario, Rol y Objetivo del Agente

### Community 5 - "Community 5"
Cohesion: 0.25
Nodes (7): 1. Visión Global del Proyecto, 2. Coordinación de Agentes, 3. Acuerdos de Integración (Contratos API), 4. Instrucción Crítica, Carpetas del Proyecto:, Instrucciones IA - Orquestador: Sistema de Detección de Fraude Bancario, Rol y Objetivo del Agente Principal

### Community 6 - "Community 6"
Cohesion: 0.25
Nodes (7): 1. Stack Tecnológico, 2. Arquitectura del Frontend, 3. Patrones de Diseño a Implementar, 4. Reglas Críticas para la Implementación (IA), code:block1 (src/app/), Instrucciones IA - Frontend: Sistema de Detección de Fraude Bancario, Rol y Objetivo del Agente

### Community 7 - "Community 7"
Cohesion: 0.4
Nodes (4): Getting Started, Guides, Maven Parent overrides, Reference Documentation

## Knowledge Gaps
- **30 isolated node(s):** `AlertaFraude`, `Cliente`, `Transaccion`, `fixture`, `compiled` (+25 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **5 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `DashboardComponent` connect `Community 0` to `Community 2`?**
  _High betweenness centrality (0.019) - this node is a cross-community bridge._
- **Why does `AlertsListComponent` connect `Community 0` to `Community 2`?**
  _High betweenness centrality (0.019) - this node is a cross-community bridge._
- **What connects `AlertaFraude`, `Cliente`, `Transaccion` to the rest of the system?**
  _30 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.14 - nodes in this community are weakly interconnected._