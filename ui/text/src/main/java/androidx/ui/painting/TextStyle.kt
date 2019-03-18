/*
* Copyright 2018 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package androidx.ui.painting

import androidx.ui.engine.text.BaselineShift
import androidx.ui.engine.text.FontStyle
import androidx.ui.engine.text.FontSynthesis
import androidx.ui.engine.text.FontWeight
import androidx.ui.engine.text.ParagraphStyle
import androidx.ui.engine.text.TextAlign
import androidx.ui.engine.text.TextBaseline
import androidx.ui.engine.text.TextDecoration
import androidx.ui.engine.text.TextDirection
import androidx.ui.engine.text.font.FontFamily
import androidx.ui.engine.window.Locale
/*import androidx.ui.foundation.diagnostics.DiagnosticLevel
import androidx.ui.foundation.diagnostics.DiagnosticPropertiesBuilder
import androidx.ui.foundation.diagnostics.Diagnosticable
import androidx.ui.foundation.diagnostics.DiagnosticsNode
import androidx.ui.foundation.diagnostics.DiagnosticsProperty
import androidx.ui.foundation.diagnostics.EnumProperty
import androidx.ui.foundation.diagnostics.FloatProperty
import androidx.ui.foundation.diagnostics.MessageProperty
import androidx.ui.foundation.diagnostics.StringProperty
import androidx.ui.foundation.diagnostics.describeIdentity*/
import androidx.ui.lerp
import androidx.ui.painting.basictypes.RenderComparison
import androidx.ui.toStringAsFixed

/*import androidx.ui.toStringAsFixed*/

private const val _kDefaultDebugLabel: String = "unknown"

/** The default font size if none is specified. */
private const val _defaultFontSize: Float = 14.0f

/**
 * An opaque object that determines the size, position, and rendering of text.
 *
 * Creates a new TextStyle object.
 *
 * * `color`: The color to use when painting the text. If this is specified, `foreground` must be null.
 * * `fontSize`: The size of glyphs (in logical pixels) to use when painting the text.
 * * `fontWeight`: The typeface thickness to use when painting the text (e.g., bold).
 * * `fontStyle`: The typeface variant to use when drawing the letters (e.g., italics).
 * * `letterSpacing`: The amount of space (in logical pixels) to add between each letter.
 * * `wordSpacing`: The amount of space (in logical pixels) to add at each sequence of white-space (i.e. between each word). Only works on Android Q and above.
 * * `textBaseline`: The common baseline that should be aligned between this text span and its parent text span, or, for the root text spans, with the line box.
 * * `baselineShift`: This parameter specifies how much the baseline is shifted from the current position.
 * * `height`: The height of this text span, as a multiple of the font size.
 * * `locale`: The locale used to select region-specific glyphs.
 * * `background`: The background color for the text.
 * * `decoration`: The decorations to paint near the text (e.g., an underline).
 * * `debugLabel`: A human-readable description of this text style.
 * * `fontFamily`: The name of the font to use when painting the text (e.g., Roboto).
 * * It is combined with the `fontFamily` argument to set the [fontFamily] property.
 */
// TODO(Migration/qqd): Implement immutable.
// @immutable
data class TextStyle(
    val inherit: Boolean? = true,
    val color: Color? = null,
    val fontSize: Float? = null,
    val fontWeight: FontWeight? = null,
    val fontStyle: FontStyle? = null,
    val fontSynthesis: FontSynthesis? = null,
    val letterSpacing: Float? = null,
    val wordSpacing: Float? = null,
    val textBaseline: TextBaseline? = null,
    val baselineShift: BaselineShift? = null,
    val height: Float? = null,
    val locale: Locale? = null,
    // TODO(Migration/haoyuchang): Changed from Paint to Color.
    val background: Color? = null,
    // TODO(Migration/qqd): The flutter version we are implementing does not have "foreground" in
    // painting/TextStyle, but has it in engine/TextStyle.
    val decoration: TextDecoration? = null,
    var fontFamily: FontFamily? = null,
    val debugLabel: String? = null
) /*: Diagnosticable*/ {

    init {
        assert(inherit != null)
    }

    /**
     * Creates a copy of this text style replacing or altering the specified properties.
     *
     * The non-numeric properties [color], [fontFamily], [decoration] replaced with the new values.
     *
     *
     * The numeric properties are multiplied by the given factors and then incremented by the given
     * deltas.
     *
     * For example, `style.apply(fontSizeFactor: 2.0, fontSizeDelta: 1.0)` would return a
     * [TextStyle] whose [fontSize] is `style.fontSize * 2.0 + 1.0`.
     *
     * For the [fontWeight], the delta is applied to the [FontWeight] enum index values, so that for
     * instance `style.apply(fontWeightDelta: -2)` when applied to a `style` whose [fontWeight] is
     * [FontWeight.w500] will return a [TextStyle] with a [FontWeight.w300].
     *
     * The numeric arguments must not be null.
     *
     * If the underlying values are null, then the corresponding factors and/or deltas must not be
     * specified.
     *
     * If [foreground] is specified on this object, then applying [color] here will have no effect.
     */
    fun apply(
        color: Color? = null,
        decoration: TextDecoration? = null,
        fontFamily: FontFamily? = null,
        fontSizeFactor: Float = 1.0f,
        fontSizeDelta: Float = 0.0f,
        fontWeightDelta: Int = 0,
        letterSpacingFactor: Float = 1.0f,
        letterSpacingDelta: Float = 0.0f,
        wordSpacingFactor: Float = 1.0f,
        wordSpacingDelta: Float = 0.0f,
        heightFactor: Float = 1.0f,
        heightDelta: Float = 0.0f
    ): TextStyle {
        assert(fontSize != null || (fontSizeFactor == 1.0f && fontSizeDelta == 0.0f))
        assert(fontWeight != null || fontWeightDelta == 0)
        assert(letterSpacing != null || (letterSpacingFactor == 1.0f && letterSpacingDelta == 0.0f))
        assert(wordSpacing != null || (wordSpacingFactor == 1.0f && wordSpacingDelta == 0.0f))

        // TODO(siyamed) remove debug labels
        var modifiedDebugLabel = if (debugLabel != null) "($debugLabel).apply" else ""

        return TextStyle(
            inherit = inherit,
            color = color ?: this.color,
            fontFamily = fontFamily ?: this.fontFamily,
            fontSize = fontSize?.let { it * fontSizeFactor + fontSizeDelta },
            fontWeight = fontWeight?.let {
                FontWeight.values[
                    (it.index + fontWeightDelta).coerceIn(0, FontWeight.values.size - 1)
                ]
            },
            fontStyle = fontStyle,
            fontSynthesis = fontSynthesis,
            letterSpacing = letterSpacing?.let { it * letterSpacingFactor + letterSpacingDelta },
            wordSpacing = wordSpacing?.let { it * wordSpacingFactor + wordSpacingDelta },
            textBaseline = textBaseline,
            baselineShift = baselineShift,
            height = height?.let { it * heightFactor + heightDelta},
            locale = locale,
            background = background,
            decoration = decoration ?: this.decoration,
            debugLabel = modifiedDebugLabel
        )
    }

    /**
     * Returns a new text style that is a combination of this style and the given [other] style.
     *
     * If the given [other] text style has its [TextStyle.inherit] set to true, its null properties
     * are replaced with the non-null properties of this text style. The [other] style
     * _inherits_ the properties of this style. Another way to think of it is that the "missing"
     * properties of the [other] style are _filled_ by the properties of this style.
     *
     * If the given [other] text style has its [TextStyle.inherit] set to false, returns the given
     * [other] style unchanged. The [other] style does not inherit properties of this style.
     *
     * If the given text style is null, returns this text style.
     */
    fun merge(other: TextStyle? = null): TextStyle {
        if (other == null) return this
        if (!other.inherit!!) return other

        // TODO(siyamed) remove debug labels
        var mergedDebugLabel = ""
        if (other.debugLabel != null || debugLabel != null) {
            mergedDebugLabel = "(${debugLabel ?: _kDefaultDebugLabel}).merge(" +
                    "${other.debugLabel ?: _kDefaultDebugLabel})"
        }

        return TextStyle(
            inherit = inherit,
            color = other.color ?: this.color,
            fontFamily = other.fontFamily ?: this.fontFamily,
            fontSize = other.fontSize ?: this.fontSize,
            fontWeight = other.fontWeight ?: this.fontWeight,
            fontStyle = other.fontStyle ?: this.fontStyle,
            fontSynthesis = other.fontSynthesis ?: this.fontSynthesis,
            letterSpacing = other.letterSpacing ?: this.letterSpacing,
            wordSpacing = other.wordSpacing ?: this.wordSpacing,
            textBaseline = other.textBaseline ?: this.textBaseline,
            baselineShift = other.baselineShift ?: this.baselineShift,
            height = other.height ?: this.height,
            locale = other.locale ?: this.locale,
            background = other.background ?: this.background,
            decoration = other.decoration ?: this.decoration,
            debugLabel = mergedDebugLabel
        )
    }

    /**
     * Interpolate between two text styles.
     *
     * This will not work well if the styles don't set the same fields.
     *
     * The `t` argument represents position on the timeline, with 0.0 meaning that the interpolation
     * has not started, returning `a` (or something equivalent to `a`), 1.0 meaning that the
     * interpolation has finished, returning `b` (or something equivalent to `b`), and values in
     * between meaning that the interpolation is at the relevant point on the timeline between `a`
     * and `b`. The interpolation can be extrapolated beyond 0.0 and 1.0, so negative values and
     * values greater than 1.0 are valid (and can easily be generated by curves such as
     * [Curves.elasticInOut]).
     *
     * Values for `t` are usually obtained from an [Animation<Float>], such as an
     * [AnimationController].
     */
    companion object {
        fun lerp(a: TextStyle? = null, b: TextStyle? = null, t: Float): TextStyle? {
            val aIsNull = a == null
            val bIsNull = b == null
            val inheritEqual = a?.inherit == b?.inherit
            assert(aIsNull || bIsNull || inheritEqual)
            if (aIsNull && bIsNull) return null
            // TODO(siyamed) remove debug labels
            var lerpDebugLabel = "lerp(${a?.debugLabel
                ?: _kDefaultDebugLabel} ⎯${t.toStringAsFixed(1)}→ ${b?.debugLabel
                ?: _kDefaultDebugLabel})"

            if (a == null) {
                val newB =
                    b?.copy(debugLabel = lerpDebugLabel)?: TextStyle(debugLabel = lerpDebugLabel)
                return if (t < 0.5) {
                    TextStyle(
                        inherit = newB.inherit,
                        color = Color.lerp(null, newB.color, t),
                        fontWeight = FontWeight.lerp(null, newB.fontWeight, t),
                        debugLabel = lerpDebugLabel
                    )
                } else {
                    newB.copy(
                        color = Color.lerp(null, newB.color, t),
                        fontWeight = FontWeight.lerp(null, newB.fontWeight, t)
                    )
                }
            }

            if (b == null) {
                return if (t < 0.5) {
                    a.copy(
                        color = Color.lerp(a.color, null, t),
                        fontWeight = FontWeight.lerp(a.fontWeight, null, t),
                        debugLabel = lerpDebugLabel
                    )
                } else {
                    TextStyle(
                        inherit = a.inherit,
                        color = Color.lerp(a.color, null, t),
                        fontWeight = FontWeight.lerp(a.fontWeight, null, t),
                        debugLabel = lerpDebugLabel
                    )
                }
            }

            // TODO(Migration/qqd): Currently [fontSize], [letterSpacing], [wordSpacing] and
            // [height] of textstyles a and b cannot be null if both a and b are not null, because
            // [lerp(Float, Float, Float)] API cannot take null parameters. We could have a
            // workaround by using 0.0, but for now let's keep it this way.
            return TextStyle(
                inherit = b.inherit,
                color = Color.lerp(a.color, b.color, t),
                fontFamily = if (t < 0.5) a.fontFamily else b.fontFamily,
                fontSize = lerp(a.fontSize ?: b.fontSize!!, b.fontSize ?: a.fontSize!!, t),
                fontWeight = FontWeight.lerp(a.fontWeight, b.fontWeight, t),
                fontStyle = if (t < 0.5) a.fontStyle else b.fontStyle,
                fontSynthesis = if (t < 0.5) a.fontSynthesis else b.fontSynthesis,
                letterSpacing = lerp(
                    a.letterSpacing ?: b.letterSpacing!!,
                    b.letterSpacing ?: a.letterSpacing!!,
                    t
                ),
                wordSpacing = lerp(
                    a.wordSpacing ?: b.wordSpacing!!,
                    b.wordSpacing ?: a.wordSpacing!!,
                    t
                ),
                textBaseline = if (t < 0.5) a.textBaseline else b.textBaseline,
                baselineShift = BaselineShift.lerp(a.baselineShift, b.baselineShift, t),
                height = lerp(a.height ?: b.height!!, b.height ?: a.height!!, t),
                locale = if (t < 0.5) a.locale else b.locale,
                background = if (t < 0.5) a.background else b.background,
                decoration = if (t < 0.5) a.decoration else b.decoration,
                debugLabel = lerpDebugLabel
            )
        }
    }

    /** The style information for text runs, encoded for use by ui. */
    fun getTextStyle(textScaleFactor: Float = 1.0f): androidx.ui.engine.text.TextStyle {
        return androidx.ui.engine.text.TextStyle(
            color = color,
            decoration = decoration,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            fontSynthesis = fontSynthesis,
            fontFamily = fontFamily,
            fontSize = if (fontSize == null) null else (fontSize * textScaleFactor),
            letterSpacing = letterSpacing,
            wordSpacing = wordSpacing,
            textBaseline = textBaseline,
            baselineShift = baselineShift,
            height = height,
            locale = locale,
            background = background
        )
    }

    /**
     * The style information for paragraphs, encoded for use by `ui`.
     * The `textScaleFactor` argument must not be null. If omitted, it defaults
     * to 1.0. The other arguments may be null. The `maxLines` argument, if
     * specified and non-null, must be greater than zero.
     *
     * If the font size on this style isn't set, it will default to 14 logical
     * pixels.
     */
    fun getParagraphStyle(
        textAlign: TextAlign? = null,
        textDirection: TextDirection? = null,
        textScaleFactor: Float = 1.0f,
        ellipsis: Boolean? = null,
        maxLines: Int? = null,
        locale: Locale? = null
    ): ParagraphStyle {
        assert(maxLines == null || maxLines > 0)
        return ParagraphStyle(
            textAlign = textAlign,
            textDirection = textDirection,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            maxLines = maxLines,
            fontFamily = fontFamily,
            fontSize = (fontSize ?: _defaultFontSize) * textScaleFactor,
            lineHeight = height,
            ellipsis = ellipsis,
            locale = locale,
            fontSynthesis = fontSynthesis
        )
    }

    /**
     * Describe the difference between this style and another, in terms of how
     * much damage it will make to the rendering.
     *
     * See also:
     *
     *  * [TextSpan.compareTo], which does the same thing for entire [TextSpan]s.
     */
    fun compareTo(other: TextStyle): RenderComparison {
        if (this == other) {
            return RenderComparison.IDENTICAL
        }
        if (inherit != other.inherit ||
            fontFamily != other.fontFamily ||
            fontSize != other.fontSize ||
            fontWeight != other.fontWeight ||
            fontStyle != other.fontStyle ||
            fontSynthesis != other.fontSynthesis ||
            letterSpacing != other.letterSpacing ||
            wordSpacing != other.wordSpacing ||
            textBaseline != other.textBaseline ||
            baselineShift != other.baselineShift ||
            height != other.height ||
            locale != other.locale ||
            background != other.background
        ) {
            return RenderComparison.LAYOUT
        }
        if (color != other.color || decoration != other.decoration) return RenderComparison.PAINT
        return RenderComparison.IDENTICAL
    }

    /*override fun toStringShort() = describeIdentity(this)

    override fun debugFillProperties(properties: DiagnosticPropertiesBuilder) {
        super.debugFillProperties(properties)
        if (debugLabel != null) {
            properties.add(MessageProperty("debugLabel", debugLabel))
        }
        var styles: MutableList<DiagnosticsNode> = mutableListOf<DiagnosticsNode>()
        styles.add(DiagnosticsProperty.create("color", color, defaultValue = null))
        styles.add(
            StringProperty(
                "family",
                fontFamily.toString(),
                defaultValue = null,
                quoted = false
            )
        )
        styles.add(FloatProperty.create("size", fontSize, defaultValue = null))
        var weightDescription = ""
        if (fontWeight != null) {
            when (fontWeight) {
                FontWeight.w100 -> weightDescription = "100"
                FontWeight.w200 -> weightDescription = "200"
                FontWeight.w300 -> weightDescription = "300"
                FontWeight.w400 -> weightDescription = "400"
                FontWeight.w500 -> weightDescription = "500"
                FontWeight.w600 -> weightDescription = "600"
                FontWeight.w700 -> weightDescription = "700"
                FontWeight.w800 -> weightDescription = "800"
                FontWeight.w900 -> weightDescription = "900"
            }
        }
        // TODO(jacobr): switch this to use enumProperty which will either cause the
        // weight description to change to w600 from 600 or require existing
        // enumProperty to handle this special case.
        styles.add(
            DiagnosticsProperty.create(
                "weight",
                fontWeight,
                description = weightDescription,
                defaultValue = null
            )
        )
        styles.add(EnumProperty<FontStyle>("style", fontStyle, defaultValue = null))
        styles.add(StringProperty("fontSynthesis", fontSynthesis?.toString(), defaultValue = null))
        styles.add(FloatProperty.create("letterSpacing", letterSpacing, defaultValue = null))
        styles.add(FloatProperty.create("wordSpacing", wordSpacing, defaultValue = null))
        styles.add(EnumProperty<TextBaseline>("baseline", textBaseline, defaultValue = null))
        styles.add(FloatProperty.create("baselineShift",
            baselineShift?.multiplier, defaultValue = null))
        styles.add(FloatProperty.create("height", height, unit = "x", defaultValue = null))
        styles.add(
            StringProperty(
                "locale",
                locale?.toString(),
                defaultValue = null,
                quoted = false
            )
        )
        styles.add(
            StringProperty(
                "background",
                background?.toString(),
                defaultValue = null,
                quoted = false
            )
        )
        if (decoration != null) {
            var decorationDescription: MutableList<String> = mutableListOf()

            // Intentionally collide with the property 'decoration' added below.
            // Tools that show hidden properties could choose the first property
            // matching the name to disambiguate.
            styles.add(
                DiagnosticsProperty.create(
                    "decoration",
                    decoration,
                    defaultValue = null,
                    level = DiagnosticLevel.hidden
                )
            )
            if (decoration != null) {
                decorationDescription.add("$decoration")
            }
            assert(decorationDescription.isNotEmpty())
            styles.add(
                MessageProperty(
                    "decoration",
                    decorationDescription.joinToString(separator = " ")
                )
            )
        }

        properties.add(
            DiagnosticsProperty.create(
                "inherit",
                inherit,
                level = DiagnosticLevel.info
            )
        )
        styles.iterator().forEach { properties.add(it) }
    } */
}