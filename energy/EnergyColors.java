/*
 * Copyright 2026 Google LLC
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

package com.google.ux.material.libmonet.energy;

import com.google.ux.material.libmonet.hct.Hct;
import com.google.ux.material.libmonet.utils.MathUtils;
import java.util.Arrays;
import java.util.List;

/** Represents the result of energy color generation, containing end and middle colors. */
public final class EnergyColors {

  private static final double CHROMA_EPSILON = 0.5;

  /** The role of the base color, used to determine hue strategy for Energy colors. */
  public enum BaseColorRole {
    PRIMARY,
    SECONDARY,
    TERTIARY,
    SURFACE,
    OTHERS
  }

  // No instance fields or constructor needed for static utility class
  private EnergyColors() {}

  /**
   * Generates Energy Colors (End, Middle, Base) using Material Theme inputs.
   *
   * @param baseColor The base color of the energy gradient.
   * @param primaryColor The Primary color of the theme.
   * @param secondaryColor The Secondary color of the theme.
   * @param tertiaryColor The Tertiary color of the theme.
   * @param surfaceColor The Surface color of the theme.
   * @param isBaseline Whether the baseline shifts are used.
   * @param baseColorRole The role of the base color to generate (Primary, Secondary, Tertiary,
   *     Surface). Can be null.
   * @return A list containing [middle, end] energy colors.
   */
  public static List<Hct> withAccents(
      Hct baseColor,
      Hct primaryColor,
      Hct secondaryColor,
      Hct tertiaryColor,
      Hct surfaceColor,
      boolean isBaseline,
      BaseColorRole baseColorRole) {
    boolean isDark = surfaceColor.getTone() <= 50.0;
    double endHue = 0.0;
    double middleHue = 0.0;

    boolean isSurface =
        baseColorRole == null
            ? isSurfaceLike(baseColor, surfaceColor)
            : baseColorRole == BaseColorRole.SURFACE;

    boolean isTertiary =
        baseColorRole == null
            ? isTertiaryLike(baseColor, primaryColor, tertiaryColor)
            : baseColorRole == BaseColorRole.TERTIARY;

    HueShifts shifts = getHueShifts(baseColor.getHue(), isBaseline);
    double midHueShift = shifts.midHueShift;
    double endHueShift = shifts.endHueShift;
    double surfaceHueShift = shifts.surfaceHueShift;

    double distPrimaryTertiary =
        MathUtils.differenceDegrees(primaryColor.getHue(), tertiaryColor.getHue());
    boolean rangeExists = distPrimaryTertiary > 10;

    double hueDir =
        Math.signum(MathUtils.rotationDirection(primaryColor.getHue(), tertiaryColor.getHue()));

    if (isBaseline) {
      hueDir *= isTertiary ? -4.3 : -1.0;
    } else if (baseColorRole != null) {
      hueDir *= isTertiary ? -1.0 : 1.0;
    } else {
      hueDir *= isTertiary && rangeExists ? -1.0 : 1.0;
    }

    if (isSurface) {
      if (isBaseline) {
        middleHue = 240.0;
        endHue = 270.0;
      } else {
        middleHue = primaryColor.getHue() + surfaceHueShift * hueDir * 0.75;
        endHue = primaryColor.getHue() + surfaceHueShift * hueDir;
      }
    } else {
      middleHue = baseColor.getHue() + midHueShift * hueDir;
      endHue = baseColor.getHue() + endHueShift * hueDir;
    }

    BaseColorRole effectiveBaseColorRole =
        baseColorRole != null
            ? baseColorRole
            : (isTertiary ? BaseColorRole.TERTIARY : BaseColorRole.OTHERS);
    Hct endColor =
        energyEndColor(
            baseColor,
            MathUtils.sanitizeDegreesDouble(endHue),
            isDark,
            isBaseline,
            effectiveBaseColorRole);
    Hct middleColor =
        energyMiddleColor(
            baseColor, endColor, MathUtils.sanitizeDegreesDouble(middleHue), isBaseline);
    return Arrays.asList(middleColor, endColor);
  }

  /**
   * Generates Energy Colors (End, Middle, Base) using Material Theme inputs.
   *
   * @param baseColor The base color of the energy gradient.
   * @param primaryColor The Primary color of the theme.
   * @param secondaryColor The Secondary color of the theme.
   * @param tertiaryColor The Tertiary color of the theme.
   * @param surfaceColor The Surface color of the theme.
   * @param isBaseline Whether the baseline shifts are used.
   * @return A list containing [middle, end] energy colors.
   */
  public static List<Hct> withAccents(
      Hct baseColor,
      Hct primaryColor,
      Hct secondaryColor,
      Hct tertiaryColor,
      Hct surfaceColor,
      boolean isBaseline) {
    return withAccents(
        baseColor, primaryColor, secondaryColor, tertiaryColor, surfaceColor, isBaseline, null);
  }

  /**
   * Generates Energy Colors (End, Middle, Base) using Material Theme inputs.
   *
   * @param baseColor The base color of the energy gradient.
   * @param primaryColor The Primary color of the theme.
   * @param secondaryColor The Secondary color of the theme.
   * @param tertiaryColor The Tertiary color of the theme.
   * @param surfaceColor The Surface color of the theme.
   * @param isBaseline Whether the baseline shifts are used.
   * @param baseColorRole The role of the base color to generate (Primary, Secondary, Tertiary,
   *     Surface). Can be null.
   * @return An int array containing [middle, end] energy colors.
   */
  public static int[] withAccents(
      int baseColor,
      int primaryColor,
      int secondaryColor,
      int tertiaryColor,
      int surfaceColor,
      boolean isBaseline,
      BaseColorRole baseColorRole) {
    List<Hct> hcts =
        withAccents(
            Hct.fromInt(baseColor),
            Hct.fromInt(primaryColor),
            Hct.fromInt(secondaryColor),
            Hct.fromInt(tertiaryColor),
            Hct.fromInt(surfaceColor),
            isBaseline,
            baseColorRole);
    return new int[] {hcts.get(0).toInt(), hcts.get(1).toInt()};
  }

  /**
   * Generates Energy Colors (End, Middle, Base) using Material Theme inputs.
   *
   * @param baseColor The base color of the energy gradient.
   * @param primaryColor The Primary color of the theme.
   * @param secondaryColor The Secondary color of the theme.
   * @param tertiaryColor The Tertiary color of the theme.
   * @param surfaceColor The Surface color of the theme.
   * @param isBaseline Whether the baseline shifts are used.
   * @return An int array containing [middle, end] energy colors.
   */
  public static int[] withAccents(
      int baseColor,
      int primaryColor,
      int secondaryColor,
      int tertiaryColor,
      int surfaceColor,
      boolean isBaseline) {
    return withAccents(
        baseColor, primaryColor, secondaryColor, tertiaryColor, surfaceColor, isBaseline, null);
  }

  /**
   * Generates energy colors based on hue rotations from a base color.
   *
   * @param baseColor The base color.
   * @param isDark Whether the theme is dark.
   * @param rotationDirection The direction of hue rotation, 1 for clockwise, -1 for
   *     counter-clockwise.
   * @param endHueRotation The hue rotation for the energy end color.
   * @param middleHueRotation The hue rotation for the energy middle color.
   * @return A list containing [middle, end] energy colors.
   */
  public static List<Hct> withHueRotations(
      Hct baseColor,
      boolean isDark,
      double rotationDirection,
      double endHueRotation,
      double middleHueRotation) {
    double direction = Math.signum(rotationDirection);
    double endHue =
        MathUtils.sanitizeDegreesDouble(baseColor.getHue() + endHueRotation * direction);
    double middleHue =
        MathUtils.sanitizeDegreesDouble(baseColor.getHue() + middleHueRotation * direction);
    return withHueValues(baseColor, isDark, endHue, middleHue);
  }

  /**
   * Generates energy colors based on hue rotations from a base color.
   *
   * @param baseColor The base color.
   * @param isDark Whether the theme is dark.
   * @param rotationDirection The direction of hue rotation, 1 for clockwise, -1 for
   *     counter-clockwise.
   * @param endHueRotation The hue rotation for the energy end color.
   * @param middleHueRotation The hue rotation for the energy middle color.
   * @return An int array containing [middle, end] energy colors.
   */
  public static int[] withHueRotations(
      int baseColor,
      boolean isDark,
      double rotationDirection,
      double endHueRotation,
      double middleHueRotation) {
    List<Hct> hcts =
        withHueRotations(
            Hct.fromInt(baseColor), isDark, rotationDirection, endHueRotation, middleHueRotation);
    return new int[] {hcts.get(0).toInt(), hcts.get(1).toInt()};
  }

  /**
   * Generates energy colors based on a base color.
   *
   * @param baseColor The base color.
   * @param isDark Whether the theme is dark.
   * @param endHue The hue of the energy end color.
   * @param middleHue The hue of the energy middle color.
   * @return A list containing [middle, end] energy colors.
   */
  public static List<Hct> withHueValues(
      Hct baseColor, boolean isDark, double endHue, double middleHue) {
    Hct endColor = energyEndColor(baseColor, endHue, isDark, false, null);
    Hct middleColor = energyMiddleColor(baseColor, endColor, middleHue, false);
    return Arrays.asList(middleColor, endColor);
  }

  /**
   * Generates energy colors based on a base color.
   *
   * @param baseColor The base color.
   * @param isDark Whether the theme is dark.
   * @param endHue The hue of the energy end color.
   * @param middleHue The hue of the energy middle color.
   * @return An int array containing [middle, end] energy colors.
   */
  public static int[] withHueValues(
      int baseColor, boolean isDark, double endHue, double middleHue) {
    List<Hct> hcts = withHueValues(Hct.fromInt(baseColor), isDark, endHue, middleHue);
    return new int[] {hcts.get(0).toInt(), hcts.get(1).toInt()};
  }

  private static class HueShifts {
    final double midHueShift;
    final double endHueShift;
    final double surfaceHueShift;

    HueShifts(double midHueShift, double endHueShift, double surfaceHueShift) {
      this.midHueShift = midHueShift;
      this.endHueShift = endHueShift;
      this.surfaceHueShift = surfaceHueShift;
    }
  }

  private static HueShifts getHueShifts(double hue, boolean isBaseline) {
    double shiftAdjust = 1.0;
    if (isBaseline) {
      if (hue <= 170.0) {
        shiftAdjust = 1.0;
      } else if (hue <= 240.0) {
        shiftAdjust = 2.0;
      } else if (hue <= 265.0) {
        shiftAdjust = 1.7;
      } else {
        shiftAdjust = 1.0;
      }
    } else {
      if (hue <= 37.0) {
        shiftAdjust = 1.5;
      } else if (hue <= 97.0) {
        shiftAdjust = 1.25;
      } else if (hue <= 160.0) {
        shiftAdjust = 0.48;
      } else if (hue <= 203.0) {
        shiftAdjust = 1.86;
      } else if (hue <= 277.0) {
        shiftAdjust = 1.14;
      } else if (hue <= 332.0) {
        shiftAdjust = 1.0;
      } else {
        shiftAdjust = 1.25;
      }
    }

    double midHueShift = 0.0;
    double endHueShift = 0.0;
    double surfaceHueShift = 0.0;

    if (isBaseline) {
      endHueShift = 16.0 * shiftAdjust;
      midHueShift = 0.25 * endHueShift;
      surfaceHueShift = 10.0;
    } else {
      midHueShift = 10.0 * shiftAdjust;
      endHueShift = 21.0 * shiftAdjust;
      surfaceHueShift = 15.0;
    }

    return new HueShifts(midHueShift, endHueShift, surfaceHueShift);
  }

  /** Generates the Energy End color given the base color and parameters. */
  private static Hct energyEndColor(
      Hct baseColor, double endHue, boolean isDark, boolean isBaseline, BaseColorRole colorRole) {
    double tone = baseColor.getTone();
    double chroma = baseColor.getChroma();

    if (baseColor.getTone() <= 20.0) {
      // Case A
      tone = baseColor.getTone() + (baseColor.getTone() < 13 ? 55.0 : 40.0);
      if (isBaseline) {
        tone = baseColor.getTone() + 25.0;
      }
      chroma = baseColor.getChroma() * 3.0;
      if (colorRole != null) {
        double chromaInterim = Math.max(chroma, baseColor.getChroma() + 8.0);
        chroma = Math.min(chromaInterim, 80.0);
      }
      if (isBaseline) {
        chroma = 43.0;
      }
    } else if (baseColor.getTone() < 86.0) {
      // Case B
      tone = Math.min(97, baseColor.getTone() + (baseColor.getTone() < 42.0 ? 30.0 : 40.0));
      double chromaInterim = Math.max(28.0, baseColor.getChroma() * 2.0);
      chroma = Math.min(chromaInterim, 60.0);
      if (isBaseline) {
        chroma = 61.0;
      }
      Hct temp = Hct.from(endHue, chroma, tone);
      if (temp.getChroma() < chroma - CHROMA_EPSILON) {
        double targetChromaValue = 60.0;
        if (isBaseline) {
          targetChromaValue = 61.0;
        }
        tone = findClosestTone(endHue, targetChromaValue, tone);
        tone = Math.max(tone, isDark ? baseColor.getTone() - 16.0 : baseColor.getTone() + 6.0);
        if (baseColor.getTone() < 73.0) {
          tone = Math.max(tone, isDark ? baseColor.getTone() + 10.0 : baseColor.getTone() + 8.0);
        }
        if (isBaseline && colorRole == BaseColorRole.TERTIARY) {
          if (baseColor.getTone() < 42.0) {
            tone = isDark ? baseColor.getTone() + 31.0 : baseColor.getTone() + 26.0;
          } else if (baseColor.getTone() < 82.0) {
            tone = isDark ? baseColor.getTone() + 7.0 : baseColor.getTone() + 9.0;
          } else {
            tone = isDark ? baseColor.getTone() - 16.0 : baseColor.getTone() + 6.0;
          }
        }
      }
    } else {
      // Case C
      tone = Math.min(97.0, baseColor.getTone() + 5.0);
      double chromaInterim =
          Math.max(28.0, Math.max(baseColor.getChroma() + 20.0, baseColor.getChroma() * 2.0));
      chroma = Math.min(chromaInterim, 80.0);
      Hct temp = Hct.from(endHue, chroma, tone);
      if (temp.getChroma() < chroma - CHROMA_EPSILON) {
        tone = findClosestTone(endHue, chroma, tone);
        tone = Math.max(tone, isDark ? baseColor.getTone() - 14.0 : baseColor.getTone() - 5.0);
        if (isBaseline && colorRole == BaseColorRole.TERTIARY) {
          tone = isDark ? baseColor.getTone() - 7.0 : baseColor.getTone() - 2.0;
        }
      }
    }
    Hct endColor = Hct.from(endHue, chroma, tone);

    if (endColor.getHue() >= 180.0 && endColor.getHue() <= 212.0 && endColor.getChroma() > 45.0) {
      endColor = Hct.from(endColor.getHue(), 45.0, endColor.getTone());
    }
    return endColor;
  }

  /** Generates the Energy Middle color given the base color, end color, and hue. */
  private static Hct energyMiddleColor(
      Hct baseColor, Hct endColor, double middleHue, boolean isBaseline) {
    double toneDiff = endColor.getTone() - baseColor.getTone();
    double tone = 0.0;

    if (toneDiff > 20.0) {
      tone = (baseColor.getTone() * 2.0) / 3.0 + (endColor.getTone() * 1.0) / 3.0;
    } else if (toneDiff < 8.0) {
      tone = (baseColor.getTone() * 1.0) / 3.0 + (endColor.getTone() * 2.0) / 3.0;
    } else {
      tone = (baseColor.getTone() * 1.0) / 2.0 + (endColor.getTone() * 1.0) / 2.0;
    }

    double chroma = (baseColor.getChroma() + endColor.getChroma()) / 2.0;
    if (isBaseline) {
      chroma = (baseColor.getChroma() * 1.0) / 3.0 + (endColor.getChroma() * 2.0) / 3.0;
    }
    return Hct.from(middleHue, chroma, tone);
  }

  private static double findClosestTone(double hue, double targetChroma, double baseTone) {
    double closestTone = -1.0;
    double minToneDiff = 1000.0;
    double maxChroma = -1.0;
    double toneWithMaxChroma = -1.0;

    // Search for the closest tone that can reach the target chroma.
    for (int t = 0; t <= 100; t++) {
      Hct testColor = Hct.from(hue, targetChroma, (double) t);
      // Track max chroma found
      if (testColor.getChroma() > maxChroma) {
        maxChroma = testColor.getChroma();
        toneWithMaxChroma = (double) t;
      }

      if (testColor.getChroma() >= targetChroma - CHROMA_EPSILON) {
        double toneDiff = Math.abs(t - baseTone);
        if (toneDiff < minToneDiff) {
          closestTone = (double) t;
          minToneDiff = toneDiff;
        }
      }
    }

    // If no tone can reach the target chroma, return the tone with the max chroma
    if (closestTone < 0) {
      return toneWithMaxChroma;
    }

    return closestTone;
  }

  private static boolean isSurfaceLike(Hct baseColor, Hct surfaceColor) {
    boolean isDark = surfaceColor.getTone() <= 50.0;
    double baseChroma = baseColor.getChroma();
    double baseTone = baseColor.getTone();
    double surfaceChroma = surfaceColor.getChroma();
    double surfaceTone = surfaceColor.getTone();

    return baseChroma < 2.5 * surfaceChroma
        && (isDark ? baseTone < 3.5 * surfaceTone : 100 - baseTone < 3.5 * (100 - surfaceTone));
  }

  private static boolean isTertiaryLike(Hct baseColor, Hct primaryColor, Hct tertiaryColor) {
    double distPrimaryTertiary =
        MathUtils.differenceDegrees(primaryColor.getHue(), tertiaryColor.getHue());

    if (distPrimaryTertiary <= 10.0 * getHueWeight(primaryColor.getHue())
        || distPrimaryTertiary <= 10.0 * getHueWeight(tertiaryColor.getHue())) {
      return false;
    }

    return MathUtils.differenceDegrees(baseColor.getHue(), tertiaryColor.getHue())
        < 15.0 * getHueWeight(baseColor.getHue());
  }

  private static double getHueWeight(double hue) {
    // Normalize hue to 0-360
    double h = ((hue % 360) + 360) % 360;

    // Define anchor points [hue, weight]
    double[][] anchors = {
      {0, 1.0}, {50, 1.0}, {90, 0.6}, {140, 0.9}, {210, 1.2}, {280, 1.5}, {320, 1.1}, {360, 1.0}
    };

    // Find the two anchors the hue falls between
    for (int i = 0; i < anchors.length - 1; i++) {
      double h1 = anchors[i][0];
      double w1 = anchors[i][1];
      double h2 = anchors[i + 1][0];
      double w2 = anchors[i + 1][1];
      if (h >= h1 && h <= h2) {
        // Linear interpolation
        double t = (h - h1) / (h2 - h1);
        return w1 + t * (w2 - w1);
      }
    }
    return 1.0;
  }
}
