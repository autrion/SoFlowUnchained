# SO2 Air Codex Specification

## Project
- **Project**: SO2 Air Codex
- **Version**: 1.0.0
- **Date**: 2025-08-19
- **Owner**: Roland Mitterbauer
- **Platform**: Android API 29–34
- **Scope**: BLE app to control the SoFlow SO2 Air Gen 2 scooter using predefined hex commands.

## Features
- BLE scan, connect, and automatic reconnect
- Handshake: Secret → Connect → optional Unlock
- Buttons: 20 km/h, 27 km/h, ECO, NORMAL, SPORT, LOCK, UNLOCK
- Hex logging with timestamps and export
- Configurable UUIDs, delays, and profiles
- Warning dialog before selecting speeds above 20 km/h
- English-only UI and code comments

## BLE
- Service UUID: `0000FFF0-0000-1000-8000-00805F9B34FB`
- Write UUID: `0000FFF1-0000-1000-8000-00805F9B34FB`
- Write type: `WRITE_NO_RESPONSE`
- MTU: 247
- Write delay: 100 ms
- Retries: 2 with backoff 250 ms, 500 ms

## Communication Protocol
- Secret code: `5A`
- Handshake: [`5A`, `D707A05A00012`]
- Commands:
  - Unlock: `D707A25A00003`
  - Lock: `D707A25A00014`
  - ECO: `D707A45A00005`
  - NORMAL: `D707A45A00016`
  - SPORT: `D707A25A00027`
  - Speed 20 km/h (preferred): `D707A90000C878`
  - Speed 20 km/h (alternate): `D707A90000C868`
  - Speed 27 km/h: `D707A900010EAF`

## UI
- Screens: Scan, Control, Settings, Logs
- Controls: BTN_20, BTN_27, BTN_ECO, BTN_NORMAL, BTN_SPORT, BTN_LOCK, BTN_UNLOCK
- States: DISCONNECTED, CONNECTING, HANDSHAKING, READY, ERROR

## Flows
- **Set_20kmh**: SECRET → HANDSHAKE → UNLOCK → SPEED_20_PREF (fallback: SECRET → HANDSHAKE → UNLOCK)
- **Set_27kmh**: SECRET → HANDSHAKE → UNLOCK → SPEED_27
- **Mode_ECO**: SECRET → HANDSHAKE → ECO
- **Mode_NORMAL**: SECRET → HANDSHAKE → NORMAL
- **Mode_SPORT**: SECRET → HANDSHAKE → SPORT
- **Lock**: SECRET → HANDSHAKE → LOCK
- **Unlock**: SECRET → HANDSHAKE → UNLOCK

## Validation
- **Success criteria**:
  - Command acknowledged <2 s
  - Five consecutive actions without error
  - No crashes during 15 min runtime
- **Negative tests**:
  - Device out of range
  - Write timeout
  - Firmware ignores speed override

## Legal
- Disclaimer: Use at your own risk. Respect local speed limits.
- Telemetry: No servers, no tracking, everything local.

## Risks
- Different UUIDs/checksums per firmware
- Controller blocks commands while driving
- Android BLE stack incompatibilities on some phones
