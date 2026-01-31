# Sfarzo

SFZ sampler plugin with a Kotlin Compose UI.

<img width="912" height="544" alt="Screenshot" src="https://github.com/user-attachments/assets/8ae24e27-8f34-454b-bae2-af0a511d1ccf" />

## Features

- SFZ sample playback via [sfizz](https://github.com/sfztools/sfizz)
- Kotlin Compose UI via [juce-cmp](https://github.com/lucianoiam/juce-cmp)
- On-screen keyboard
- VU meter with dB scale
- Standalone and AU plugin formats

## Requirements

- macOS (Apple Silicon or Intel)
- CMake 3.22+
- JDK 17+

## Build

```bash
cmake -B build
cmake --build build
```

Outputs:
- `build/Sfarzo_artefacts/Debug/Standalone/Sfarzo.app`
- `build/Sfarzo_artefacts/Debug/AU/Sfarzo.component`

## License

MIT
