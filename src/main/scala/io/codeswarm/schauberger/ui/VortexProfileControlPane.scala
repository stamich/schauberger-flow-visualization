package io.codeswarm.schauberger.ui

import io.codeswarm.schauberger.model._
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.{ComboBox, Label}
import scalafx.scene.layout.VBox

/** Selects the vortex profile and edits its parameters, including counter-rotating presets.
 *
 * The editing rules live in [[VortexProfileEdits]]; this pane only wires them to controls and shows the
 * fields that belong to the selected profile kind.
 */
final class VortexProfileControlPane(
                                      controller: SimulationController,
                                      onStatusRefresh: () => Unit
                                    ) extends VBox {

  spacing = 6.0
  padding = Insets(8.0)

  private def profile: VortexProfileParameters = controller.simulationParameters.swirl.profile

  private val kindCombo = new ComboBox[String](ObservableBuffer(VortexProfileEdits.KindLabels: _*)) {
    value = VortexProfileEdits.kindLabel(controller.simulationParameters.swirl.profile)
    prefWidth = 170.0
  }

  private val presetCombo = new ComboBox[String](ObservableBuffer(VortexProfileEdits.Presets.map(_.name): _*)) {
    promptText = "Choose a preset"
    prefWidth = 170.0
  }

  private val rankineCoreField = new NumericSliderField("Rankine core ratio:", 0.05, 1.0, VortexProfileEdits.rankineCore(profile), 0.15, 2,
    onValueChanged = value => edit(VortexProfileEdits.withRankineCore(_, value)))

  private val lambCoreField = new NumericSliderField("Lamb core ratio:", 0.05, 1.0, VortexProfileEdits.lambCore(profile), 0.15, 2,
    onValueChanged = value => edit(VortexProfileEdits.withLambCore(_, value)))

  private val lambCirculationField = new NumericSliderField("Lamb circulation:", 0.0, 3.0, VortexProfileEdits.lambCirculation(profile), 0.5, 2,
    onValueChanged = value => edit(VortexProfileEdits.withLambCirculation(_, value)))

  private val innerStrengthField = new NumericSliderField("Inner strength:", 0.0, 2.0, VortexProfileEdits.counterRotating(profile).innerStrength, 0.5, 2,
    onValueChanged = value => edit(VortexProfileEdits.withInnerStrength(_, value)))

  private val outerStrengthField = new NumericSliderField("Outer (reverse) strength:", 0.0, 2.0, VortexProfileEdits.counterRotating(profile).outerStrength, 0.5, 2,
    onValueChanged = value => edit(VortexProfileEdits.withOuterStrength(_, value)))

  private val transitionRadiusField = new NumericSliderField("Transition radius:", 0.1, 0.9, VortexProfileEdits.counterRotating(profile).transitionRadius, 0.2, 2,
    onValueChanged = value => edit(VortexProfileEdits.withTransitionRadius(_, value)))

  private val transitionWidthField = new NumericSliderField("Transition width:", 0.01, 0.4, VortexProfileEdits.counterRotating(profile).transitionWidth, 0.1, 2,
    onValueChanged = value => edit(VortexProfileEdits.withTransitionWidth(_, value)))

  private val counterRotatingFields = Seq(innerStrengthField, outerStrengthField, transitionRadiusField, transitionWidthField)

  children = Seq(
    new Label("Vortex profile"),
    kindCombo,
    new Label("Counter-rotating preset:"),
    presetCombo,
    rankineCoreField,
    lambCoreField,
    lambCirculationField,
    innerStrengthField,
    outerStrengthField,
    transitionRadiusField,
    transitionWidthField
  )

  showFieldsFor(profile)

  kindCombo.value.onChange { (_, _, next) =>
    applyProfile(VortexProfileEdits.switchKind(profile, next))
  }

  presetCombo.value.onChange { (_, _, next) =>
    VortexProfileEdits.preset(next).foreach(preset => applyProfile(preset.parameters))
  }

  /** Applies an edit to the current profile and refreshes the status line. */
  private def edit(change: VortexProfileParameters => VortexProfileParameters): Unit = applyProfile(change(profile))

  /** Stores a new profile, keeps the controls in step with it and shows only the fields it uses. */
  private def applyProfile(next: VortexProfileParameters): Unit = {
    val swirl = controller.simulationParameters.swirl
    controller.updateSimulationParameters(controller.simulationParameters.copy(swirl = swirl.copy(profile = next)))
    syncControls(next)
    showFieldsFor(next)
    onStatusRefresh()
  }

  /** Moves the sliders to the stored values without notifying, so presets and kind switches show what is active. */
  private def syncControls(next: VortexProfileParameters): Unit = {
    rankineCoreField.setValue(VortexProfileEdits.rankineCore(next))
    lambCoreField.setValue(VortexProfileEdits.lambCore(next))
    lambCirculationField.setValue(VortexProfileEdits.lambCirculation(next))
    val composite = VortexProfileEdits.counterRotating(next)
    innerStrengthField.setValue(composite.innerStrength)
    outerStrengthField.setValue(composite.outerStrength)
    transitionRadiusField.setValue(composite.transitionRadius)
    transitionWidthField.setValue(composite.transitionWidth)
    val label = VortexProfileEdits.kindLabel(next)
    if (kindCombo.value.value != label) kindCombo.value = label
  }

  /** Shows the fields of the selected kind and hides the rest. */
  private def showFieldsFor(current: VortexProfileParameters): Unit = {
    val composite = current.isInstanceOf[CounterRotatingParameters]
    setShown(rankineCoreField, current.isInstanceOf[RankineParameters] ||
      (composite && current.asInstanceOf[CounterRotatingParameters].inner.isInstanceOf[RankineParameters]))
    setShown(lambCoreField, current.isInstanceOf[LambOseenParameters])
    setShown(lambCirculationField, current.isInstanceOf[LambOseenParameters])
    setShown(presetCombo, composite)
    counterRotatingFields.foreach(setShown(_, composite))
  }

  private def setShown(node: scalafx.scene.Node, shown: Boolean): Unit = {
    node.visible = shown
    node.managed = shown
  }
}
