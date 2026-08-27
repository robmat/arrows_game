package com.batodev.arrows

import kotlin.math.PI

object GameConstants {
    // ====================
    // GAME ENGINE DEFAULTS
    // ====================
    const val INITIAL_LIVES = 5
    const val LOST_LIFE_ALPHA = 0.3f

    // ====================
    // GAME PROGRESSION
    // ====================
    const val GENERATOR_UNLOCK_LEVEL = 20

    // ====================
    // CUSTOM GENERATOR UI
    // ====================
    const val GENERATOR_MIN_SIZE = 20f
    const val GENERATOR_MAX_SIZE = 100f
    const val GENERATOR_MAX_SIZE_FILL_BOARD = 35f
    const val GENERATOR_DEFAULT_SIZE = 35f
    const val SHAPE_TYPE_RECTANGULAR = "rectangular"
    const val SHAPE_ICON_SIZE = 32

    // ====================
    // HOME SCREEN ANIMATIONS
    // ====================
    const val HOME_ENTER_ANIM_DURATION = 400
    const val HOME_STAGGER_DELAY_MS = 120
    const val HOME_ENTER_OFFSET_DP = 60f
    const val HOME_LOGO_PULSE_DURATION = 1800
    const val HOME_LOGO_PULSE_SCALE = 1.12f
    const val HOME_BUTTON_PULSE_DURATION = 1400
    const val HOME_BUTTON_PULSE_SCALE = 1.03f

    // ====================
    // SETTINGS SCREEN ANIMATIONS
    // ====================
    const val SETTINGS_ENTER_ANIM_DURATION = 350
    const val SETTINGS_STAGGER_DELAY_MS = 100
    const val SETTINGS_ENTER_OFFSET_DP = 80f

    // ====================
    // GENERATOR SCREEN ANIMATIONS
    // ====================
    const val GENERATOR_ENTER_ANIM_DURATION = 350
    const val GENERATOR_STAGGER_DELAY_MS = 80
    const val GENERATOR_ENTER_OFFSET_DP = 40f
    const val GENERATOR_BUTTON_PULSE_DURATION = 1200
    const val GENERATOR_BUTTON_PULSE_SCALE = 1.04f
    const val GENERATOR_VALUE_SCALE_TARGET = 1.3f
    const val GENERATOR_VALUE_SCALE_HOLD_MS = 80L
    const val GENERATOR_COLOR_ANIM_DURATION = 200
    const val GENERATOR_SHAPE_SELECTED_SCALE = 1.08f
    const val GENERATOR_SHAPE_POP_IN_STAGGER_MS = 40L

    // ====================
    // BOARD IMAGE PROCESSING
    // ====================
    const val COLOR_THRESHOLD = 128
    const val ALPHA_SHIFT = 24
    const val RED_SHIFT = 16
    const val GREEN_SHIFT = 8
    const val COLOR_MASK = 0xFF

    // ====================
    // LEVEL GENERATION ENGINE
    // ====================
    const val DEFAULT_STRAIGHT_PREFERENCE = 0.90f
    const val MAX_FILL_BOARD_SIZE = 35
    const val PROGRESS_FACTOR = 1f
    const val FIRST_SNAKE_MAX_ATTEMPTS = 100

    // ====================
    // LEVEL PROGRESSION
    // ====================
    const val BASE_BOARD_SIZE = 5
    const val SIZE_REDUCTION_PER_STEP = 3
    const val LIVES_REDUCTION_PER_STEP = 1
    const val LEVELS_PER_PROGRESSION_STEP = 10
    const val DEFAULT_INITIAL_LIVES = 5
    const val MIN_SNAKE_LENGTH_BASE = 3
    const val MIN_SNAKE_LENGTH_MIN = 4
    const val MIN_SNAKE_LENGTH_MAX = 30

    // ====================
    // LEVEL MANAGER (SHAPE PROBABILITY)
    // ====================
    const val MIN_BOARD_SIZE_FOR_SHAPES = 20
    const val MAX_BOARD_SIZE_FOR_ALWAYS_SHAPE = 100
    const val BASE_SHAPE_PROBABILITY = 0.5f

    // ====================
    // ZOOM & PAN
    // ====================
    const val DEFAULT_SCALE = 0.94f
    const val MIN_SCALE = 0.2f
    const val MAX_SCALE = 6f

    // ====================
    // GAME FLOW & ANIMATIONS
    // ====================
    const val GAME_WON_EXIT_DELAY = 3000L
    const val GAMES_BETWEEN_INTERSTITIALS = 5
    const val GUIDANCE_ANIM_DURATION = 500
    const val PROGRESS_ANIM_DURATION = 500
    const val PROGRESS_BAR_WIDTH = 200
    const val DEBUG_CIRCLE_RADIUS = 20f
    const val PERCENT_MULTIPLIER = 100

    // ====================
    // CONFETTI CELEBRATION ANIMATION
    // ====================
    const val CONFETTI_MAX_SPEED = 30f
    const val CONFETTI_DAMPING = 0.9f
    const val CONFETTI_SPREAD = 360
    const val CONFETTI_DURATION_MS = 100L
    const val CONFETTI_EMITTER_MAX = 100
    const val CONFETTI_REL_X = 0.5
    const val CONFETTI_REL_Y = 0.3
    const val CONFETTI_COLOR_1 = 0xfce18a
    const val CONFETTI_COLOR_2 = 0xff726d
    const val CONFETTI_COLOR_3 = 0xf4306d
    const val CONFETTI_COLOR_4 = 0xb48def

    // ====================
    // TAP ANIMATION
    // ====================
    const val RIPPLE_DURATION = 300
    const val RIPPLE_MAX_RADIUS = 40f

    // ====================
    // SNAKE REMOVAL ANIMATION
    // ====================
    const val REMOVAL_FRAME_DELAY_MS = 16L
    const val REMOVAL_DURATION_HIGH = 300L
    const val REMOVAL_DURATION_MEDIUM = 600L
    const val REMOVAL_DURATION_LOW = 900L

    // ====================
    // BOARD ENTRY ANIMATIONS
    // ====================
    const val BOARD_ENTRY_SCALE_FROM = 0.92f
    const val SNAKE_ENTRY_DURATION_MS = 450L
    const val SNAKE_ENTRY_STAGGER_MS = 50L

    // ====================
    // BOARD RENDERING & VISUALS
    // ====================
    const val BOARD_BORDER_WIDTH = 2f
    const val GUIDANCE_LINE_ALPHA_FACTOR = 0.4f
    const val GUIDANCE_DASH_ON = 10f
    const val GUIDANCE_DASH_OFF = 10f
    const val TAP_AREA_ALPHA = 0.3f
    const val SINGLE_BLOCK_TAIL_FACTOR = 0.2f
    const val ARROW_HEAD_SIZE_FACTOR = 0.2f
    const val BOARD_STROKE_WIDTH_FACTOR = 0.15f
    const val BOARD_CORNER_RADIUS_FACTOR = 0.3f
    const val SNAKE_MOVE_DIST_FACTOR = 1.2f
    const val ARROW_HEAD_STROKE_WIDTH_FACTOR = 0.3f
    const val FLASH_DURATION_MS = 3000L
    const val FLASH_PULSE_DURATION = 250
    const val FLASH_MIN_ALPHA = 0.2f
    const val ARROW_HEAD_CENTER_FACTOR = 0.5f

    // ====================
    // ARROW DIRECTION ANGLES
    // ====================
    const val ANGLE_UP = 270.0
    const val ANGLE_DOWN = 90.0
    const val ANGLE_LEFT = 180.0
    const val ANGLE_RIGHT = 0.0
    const val ANGLE_TRIANGLE_OFFSET = 2.094
    const val DEG_TO_RAD = PI / 180.0

    // ====================
    // VIEW MODEL
    // ====================
    const val STOP_TIMEOUT_MILLIS = 5000L

    // ====================
    // USER PREFERENCES DEFAULTS
    // ====================
    const val DEFAULT_LEVEL = 1
    const val DEFAULT_LIVES = 5

    // ====================
    // ADS
    // ====================
    const val REQUIRED_AD_COUNT_FOR_AD_FREE = 30

    // ====================
    // EXTERNAL LINKS
    // ====================
    const val GITHUB_REPO_URL = "https://github.com/robmat/arrows_game"

    // ====================
    // INPUT HANDLING
    // ====================
    const val CELL_CENTER = 0.5f
    const val TAP_AREA_OFFSET_FACTOR = 0.3f
    const val DEFAULT_TOLERANCE = 1.3f

    // ====================
    // WIN CELEBRATION VIDEO
    // ====================
    const val VIDEO_FADE_IN_DURATION = 1000
    const val VIDEO_DISPLAY_DURATION = 3000
    const val VIDEO_FADE_OUT_DURATION = 1000
    const val WIN_VIDEOS_COUNT = 26
    const val VIDEO_PREPARATION_DELAY = 50L
    const val CONGRATULATIONS_FONT_SIZE = 32
}
