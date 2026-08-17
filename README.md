# Aniversários 🎂

App Android nativa para guardar aniversários, com um **widget** para o ecrã
principal que mostra sempre o **próximo aniversário** e quantos dias faltam.

## Funcionalidades

- Adicionar aniversários (nome, dia, mês e ano opcional).
- Lista ordenada pela proximidade do próximo aniversário.
- Mostra a idade que a pessoa vai completar (quando o ano é conhecido).
- Trata o 29 de Fevereiro em anos não bissextos (usa 28 de Fevereiro).
- Apagar aniversários.
- **Widget de ecrã principal** com o próximo aniversário, a data e a contagem
  decrescente ("É hoje!", "É amanhã", "faltam N dias").
- O widget atualiza-se automaticamente à meia-noite, ao mudar de fuso horário,
  no arranque do dispositivo e sempre que a lista muda na app.

## Stack técnica

| Camada        | Tecnologia                     |
|---------------|--------------------------------|
| Linguagem     | Kotlin                         |
| UI da app     | Jetpack Compose (Material 3)   |
| Widget        | Jetpack Glance                 |
| Persistência  | Room                           |
| Arquitetura   | MVVM (ViewModel + Repository)  |

## Estrutura

```
app/src/main/java/com/nunofrancisco/birthdaywidget/
├── MainActivity.kt              # Activity + Compose host
├── data/                        # Room: entidade, DAO, base de dados, repositório
├── util/BirthdayCalculator.kt   # Cálculo do próximo aniversário / dias em falta
├── ui/                          # Ecrã Compose, ViewModel e tema
└── widget/                      # Widget Glance, receiver e updater
```

## Como compilar

Requer o Android SDK (via Android Studio).

```bash
./gradlew assembleDebug        # gera o APK de debug
./gradlew installDebug         # instala num dispositivo/emulador ligado
```

O APK fica em `app/build/outputs/apk/debug/app-debug.apk`.

## Como usar o widget

1. Instalar e abrir a app; adicionar pelo menos um aniversário.
2. No ecrã principal, manter premido → **Widgets** → **Aniversários**.
3. Arrastar o widget "Próximo Aniversário" para o ecrã.

Tocar no widget abre a app.

- **minSdk:** 26 (Android 8.0) · **targetSdk / compileSdk:** 35
