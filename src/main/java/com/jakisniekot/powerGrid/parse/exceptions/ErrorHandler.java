package com.jakisniekot.powerGrid.parse.exceptions;

import com.jakisniekot.powerGrid.MainPlugin;

public class ErrorHandler {

    private MainPlugin plugin;

    public ErrorHandler(MainPlugin plugin) {
        this.plugin = plugin;
    }

    public void print(Error error, String ID) {

        String message = "&cERROR";

        switch (error) {
            case ItemParseError e: {
                switch (e) {
                    case FATAL_ERROR -> message = plugin.lang().getString("parsing.item.fatal");

                    case MATERIAL_ERROR -> message = plugin.lang().getString("parsing.item.nullMaterial");
                    case ITEM_TYPE_ERROR -> message = plugin.lang().getString("parsing.item.powerItemTypeError");
                    case ITEM_NAME_ERROR -> message = plugin.lang().getString("parsing.item.itemNameError");

                    case NONE -> message = plugin.lang().getString("parsing.item.success");
                }
                break;
            }

            case RecipeParseError e: {
                switch (e) {
                    case DEFAULTS_NOT_LOADED -> message = plugin.lang().getString("parsing.recipe.fatal.defaultsNotLoaded");
                    case FOLDER_ERROR -> message = plugin.lang().getString("parsing.recipe.fatal.folderError");

                    case DUPED_RECIPE_NAME -> message = plugin.lang().getString("parsing.recipe.dupedRecipeName");
                    case INPUT_LIST_ITEM -> message = plugin.lang().getString("parsing.recipe.inputItemsError");
                    case OUTPUT_LIST_ITEM -> message = plugin.lang().getString("parsing.recipe.outputItemsError");

                    case PROCESS_TIME -> message = plugin.lang().getString("parsing.recipe.noProcessTime");

                    case NONE -> message = plugin.lang().getString("parsing.recipe.success");
                }
                break;
            }

            case SoundParseError e: {
                switch (e) {
                    case NO_SOUND_TYPE -> message = plugin.lang().getString("parsing.sound.fatal.noSoundType");

                    case NO_PITCH -> message = plugin.lang().getString("parsing.sound.noPitchSet");
                    case NO_VOLUME -> message = plugin.lang().getString("parsing.sound.noVolumeSet");
                    case NO_CATEGORY -> message = plugin.lang().getString("parsing.sound.noSoundCategory");
                    case NO_PLAY_TYPE -> message = plugin.lang().getString("parsing.sound.noPlayType");

                    case NONE -> message = plugin.lang().getString("parsing.sound.success");
                }

                break;
            }

            case MachineParseError e: {
                switch (e) {
                    case ITEM_SLOT_DUPED_ID -> message = plugin.lang().getString("parsing.machine.fatal.itemSlotDupedID");
                    case ITEM_SLOT_NO_ID -> message = plugin.lang().getString("parsing.machine.fatal.itemSlotNoID");

                    case NO_ITEM_SLOT_TYPE -> message = plugin.lang().getString("parsing.machine.noItemSlotType");

                    case ITEM_STORAGE_NULL -> message = plugin.lang().getString("parsing.machine.noSection");
                    case NO_INPUTS -> message = plugin.lang().getString("parsing.machine.noInputs");
                    case NO_OUTPUTS -> message = plugin.lang().getString("parsing.machine.noOutputs");
                    case INPUT_TRANSFER_ERROR -> message = plugin.lang().getString("parsing.machine.noMaxInputTransfer");
                    case OUTPUT_TRANSFER_ERROR -> message = plugin.lang().getString("parsing.machine.noMaxOutputTransfer");

                    case ENERGY_CUSTOM -> message = plugin.lang().getString("parsing.machine.customEnergyAmount");
                    case MAX_ENERGY_ERROR-> message = plugin.lang().getString("parsing.machine.noMaxEnergy");

                    case NONE -> message = plugin.lang().getString("parsing.machine.success");
                }

                break;
            }

            default:
                break;
        }

        switch (error.getLevel().toString()) {
            case "WARNING":
                message = plugin.lang().getString("parsing.level.warning") + message;
                break;
            case "SEVERE":
                message = plugin.lang().getString("parsing.level.severe") + message;
                break;
            case "FINE":
                message = plugin.lang().getString("parsing.level.info") + message;
            case null, default:
                break;
        }

        if (ID == null) ID = "null";
        message = message.replace("{ID}", ID);

        plugin.printTextLogs(message);
    }

}
