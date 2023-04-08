package net.apcsimple.controlapplication.converters

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import net.apcsimple.controlapplication.model.processmodels.ProcessModel
import net.apcsimple.controlapplication.model.processmodels.statespace.StateSpaceModel
import sklog.KotlinLogging

private var logger = KotlinLogging.logger {}

class ModelFromJsonAdapter: StdDeserializer<ProcessModel>(ProcessModel::class.java) {

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ProcessModel? {
        val tree = p!!.readValueAsTree<ObjectNode>()
        logger.warning(tree.toPrettyString())

        if (!tree.has("id")) {
            logger.error("Model id not found")
            return null
        }
        val id = tree.get("id").asInt()
        logger.warning("ID is $id.")
        val model = ProcessModel("Model", ProcessModel.STATE_SPACE, id)
        model.structure = StateSpaceModel()
        return model
    }
}