# API Key Configuration

The Claude API key is now securely configured and **will not be committed to version control**.

## Setup for New Users

If someone clones this repository, they need to:

1. **Copy the template file:**
   ```bash
   cp local.properties.example local.properties
   ```

2. **Edit `local.properties` and add their API key:**
   ```properties
   claude.api.key=YOUR_ACTUAL_API_KEY_HERE
   ```

3. **Get an API key from:**
   https://console.anthropic.com/

4. **Rebuild the project:**
   ```bash
   ./gradlew clean assembleDebug
   ```

## How It Works

- **`local.properties`** stores the actual API key (gitignored, never committed)
- **`local.properties.example`** is a template (committed to repo)
- **`BuildConfig.CLAUDE_API_KEY`** is generated at build time from `local.properties`
- **`Config.kt`** reads from `BuildConfig.CLAUDE_API_KEY`

## Files Changed

✅ **Not Committed (Secure):**
- `local.properties` - Contains your actual API key

✅ **Committed (Safe):**
- `local.properties.example` - Template with placeholder
- `app/build.gradle.kts` - Reads from local.properties
- `Config.kt` - Uses BuildConfig instead of hardcoded key

## Security Benefits

✅ API key never appears in source code
✅ API key never committed to version control
✅ Each developer uses their own API key
✅ Easy to rotate keys (just update local.properties)
✅ Safe to push to public GitHub

## Troubleshooting

**Error: "Please set your Claude API key in Config.kt"**
- Your `local.properties` file is missing or doesn't have `claude.api.key`
- Copy from `local.properties.example` and add your real key

**Error: "Unresolved reference: BuildConfig"**
- Run `./gradlew clean build` to regenerate BuildConfig
- Make sure `buildFeatures { buildConfig = true }` is in app/build.gradle.kts

**API key not working after adding to local.properties:**
- Rebuild the project: `./gradlew clean assembleDebug`
- BuildConfig is generated at build time, not runtime
