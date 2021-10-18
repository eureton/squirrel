# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]
### Changed

## [0.2.0] - 2021-10-18
### Added
- `node/not-leaf?` shorthand

### Changed
- lazy `map`
- lazy `filter`
- `node/*leaf?*`:
  - rename to `node/leaf?`
  - make static

## [0.1.0] - 2021-09-15
### Added
- `map`, `filter`/`remove`, `reduce`
- path to override node structure via dynamic vars:
  - `tree/*identity*`
  - `node/*data-readf*`
  - `node/*data-writef*`
  - `node/*children-readf*`
  - `node/*children-writef*`
  - `node/*leaf?*`

[Unreleased]: https://github.com/eureton/squirrel/compare/0.2.0...HEAD
[0.2.0]: https://github.com/eureton/squirrel/compare/0.1.0...0.2.0
[0.1.0]: https://github.com/eureton/squirrel/compare/...0.1.0
