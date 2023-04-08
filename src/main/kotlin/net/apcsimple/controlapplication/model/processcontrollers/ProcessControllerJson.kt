package net.apcsimple.controlapplication.model.processcontrollers

data class ProcessControllerJson(
    /**
     * Used to identify the controller in the list. Must be unique.
     */
    var name: String = "Name",
    /**
     * Type of the controller.
     */
    val type: String? = null,
    /**
     * Controller running status.
     */
    var running: Boolean = false,
) {

}