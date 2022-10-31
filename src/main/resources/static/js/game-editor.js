let flags = [];
let conditions = [];

let gameId;
let gameTree;

const DESC_INDEX = 0; // index in the jstree node 'data' array
const COND_INDEX = 1; // index in the jstree node 'data' array

function initGameEditor(game, iconUrls) {
    gameId = game.id;

    if (game.rootNode !== null) {
        convertToJstreeNodesFormat(game.rootNode);
        createNamesForConditionNodes(game.rootNode);

        flags = getAllFlags(game.rootNode);
        conditions = getAllConditions(game.rootNode);

        initJstree(game.rootNode, iconUrls);

        showEditInfoButton();
        showPlayButton();
    } else {
        initJstree(null, iconUrls);

        hideEditInfoButton();
        hidePlayButton();

        showAddMainRoomModal();
    }
}

function initJstree(rootNode, iconUrls) {
    gameTree = $('#jstree')
        .jstree(
            {
                "core": {
                    "animation": 0,
                    "data": rootNode,
                    "check_callback": function () {
                        return true;
                    },
                },

                "types": {
                    "#": {
                        "max_children": 1
                    },
                    "ROOM": {
                        "icon": iconUrls.roomIcon
                    },
                    "CHOICE": {
                        "icon": iconUrls.choiceIcon
                    },
                    "FLAG": {
                        "icon": iconUrls.flagIcon
                    },
                    "CONDITION": {
                        "icon": iconUrls.conditionIcon
                    },
                },
                "plugins":
                    ["contextmenu", "state", "types", "wholerow", "sort"],

                "sort": jstreeSort,
                "contextmenu": {items: customMenu}
            })
        .bind("loaded.jstree", function () {
            $(this).jstree("open_all");
        });
}

function jstreeSort(a, b) {
    const a1 = this.get_node(a);
    const b1 = this.get_node(b);

    if (a1.type === NODE_TYPE.CONDITION && b1.type === NODE_TYPE.ROOM) {
        return -1; // show ROOM node after CONDITION nodes
    }
}

function customMenu(node) {
    let items = {
        addRoom: {
            label: ADD_ROOM_STRING,
            action: onAddRoomMenuClick(node)
        },
        addChoice: {
            label: ADD_CHOICE_STRING,
            action: onAddChoiceMenuClick(node)
        },
        addCondition: {
            label: ADD_CONDITION_STRING,
            action: onAddConditionMenuClick(node)
        },
        editItem: {
            label: EDIT_ITEM_STRING,
            action: onEditItemMenuClick(node)
        },
        deleteItem: {
            label: DELETE_ITEM_STRING,
            action: onDeleteItemMenuClick(node)
        }
    };

    switch (node.type) {
        case NODE_TYPE.CHOICE:
        case NODE_TYPE.CONDITION:
            delete items.addChoice;

            if (hasRoomAmongChildren(node)) {
                delete items.addRoom;
            }
            break;

        case NODE_TYPE.FLAG:
            delete items.addChoice;
            delete items.addCondition;
            delete items.addRoom;
            break;

        case NODE_TYPE.ROOM:
            delete items.addRoom;
            delete items.addCondition;

            if (node.parent === "#") { // if the node is a root node
                delete items.deleteItem;
            }
            break;
    }

    if (flags.length === 0) {
        delete items.addCondition;
    }

    return items;
}

function onAddRoomMenuClick(node) {
    return function () {
        showAddRoomModal(node);
    };
}

function onAddChoiceMenuClick(node) {
    return function () {
        showAddChoiceModal(node);
    };
}

function onAddConditionMenuClick(node) {
    return function () {
        showAddConditionModal(node);
    };
}

function onEditItemMenuClick(node) {
    return function () {
        switch (node.type) {
            case NODE_TYPE.ROOM:
                showEditRoomModal(node);
                break;

            case NODE_TYPE.CHOICE:
            case NODE_TYPE.FLAG:
                showEditChoiceModal(node);
                break;

            case NODE_TYPE.CONDITION:
                showEditConditionModal(node);
                break;
        }
    };
}

function onDeleteItemMenuClick(node) {
    const alertWarning = node.type === NODE_TYPE.FLAG ?
        FLAG_DELETE_ALERT_STRING : ARE_YOU_SURE_STRING;

    return function () {
        showAlertModal(DELETE_ITEM_STRING, alertWarning,
            onConfirmingToDeleteNode(node), ALERT_MODE.QUESTION);
    };
}

function onConfirmingToDeleteNode(node) {
    return function () {
        deleteNode(node);
    };
}

function showAddMainRoomModal() {
    preventClosingModal();

    showModal(function () {
        prepareModalForAddingRoomNode();

        setModalSaveButtonOnClick(function () {
            clearErrorsInModal();

            const name = getNameValueFromModal();
            const description = getDescValueFromModal();

            const node = new GameNode(name, description, NODE_TYPE.ROOM);

            createNode(null, node);

            showEditInfoButton();
            showPlayButton();
        });
    });
}

function showAddRoomModal(node) {
    showModal(function () {
        prepareModalForAddingRoomNode();

        setModalSaveButtonOnClick(function () {
            clearErrorsInModal();

            const name = getNameValueFromModal();
            const description = getDescValueFromModal();

            const roomNode = new GameNode(name, description, NODE_TYPE.ROOM);

            createNode(node.id, roomNode);
        });
    });
}

function showAddChoiceModal(node) {
    showModal(function () {
        prepareModalForAddingChoiceNode();

        setModalSaveButtonOnClick(function () {
            clearErrorsInModal();

            const name = getNameValueFromModal();

            const choiceNode = new GameNode(name, null,
                isModalCheckboxChecked() ? NODE_TYPE.FLAG :
                    NODE_TYPE.CHOICE);

            createNode(node.id, choiceNode);
        });
    });
}

function showAddConditionModal(node) {
    showModal(function () {
        prepareModalForAddingConditionNode(flags);

        setModalSaveButtonOnClick(function () {
            clearErrorsInModal();

            const flagNodeId = getSelectValueFromModal();
            let flagState = getModalRadioValue();

            const conditionName = createConditionName(
                getJstreeNode(flagNodeId), flagState);

            const conditionNode = new GameNode(conditionName, null,
                NODE_TYPE.CONDITION);

            conditionNode.data[COND_INDEX] = new Condition(flagNodeId,
                flagState);

            createNode(node.id, conditionNode);
        });
    });
}

function showEditRoomModal(node) {
    showModal(function () {
        prepareModalForEditingRoomNode(node);

        setModalSaveButtonOnClick(function () {
            clearErrorsInModal();

            const name = getNameValueFromModal();
            const description = getDescValueFromModal();

            const nodeForEdit = getJstreeNode(node.id);

            renameNode(nodeForEdit, name, description);
        });
    });
}

function showEditChoiceModal(node) {
    showModal(function () {
        prepareModalForEditingChoiceNode(node);

        setModalSaveButtonOnClick(function () {
            clearErrorsInModal();

            const name = getNameValueFromModal();

            const nodeForEdit = getJstreeNode(node.id);

            renameNode(nodeForEdit, name);
        });
    });
}

function showEditConditionModal(node) {
    showModal(function () {
        prepareModalForEditingConditionNode(node, flags);

        setModalSaveButtonOnClick(function () {
            clearErrorsInModal();

            const nodeForEdit = getJstreeNode(node.id);

            const selectedFlagId = getSelectValueFromModal();
            const selectedFlagState = getModalRadioValue();

            const conditionName = createConditionName(
                getJstreeNode(selectedFlagId), selectedFlagState);

            nodeForEdit.data[COND_INDEX] = new Condition(selectedFlagId,
                selectedFlagState);

            renameNode(nodeForEdit, conditionName);
        });
    });
}

function showCreateGameModal() {
    preventClosingModal();

    showModal(function () {
        prepareModalForCreatingGame();

        setModalSaveButtonOnClick(function () {
            clearErrorsInModal();

            let name = getNameValueFromModal();
            let desc = getDescValueFromModal();
            let lang = getSelectValueFromModal();

            makeAjaxRequest(HTTP_REQUEST_TYPE.POST, "/api/games/",
                {name: name, description: desc, language: lang},
                createGameCallback);
        });
    });
}

function createGameCallback(response) {
    if (response.responseJSON.hasErrors === true) {
        showModalFieldErrors(response.responseJSON.fieldErrors);
    } else {
        location.href = "/games/" + response.responseJSON +
            "/edit";
    }
}

function showUpdateGameInfoModal(gameId, gameName, gameDescription,
                                 gameLanguage) {
    showModal(function () {
        prepareModalForUpdatingGameInfo(gameName, gameDescription,
            gameLanguage);

        setModalSaveButtonOnClick(function () {
            clearErrorsInModal();

            const name = getNameValueFromModal();
            const desc = getDescValueFromModal();
            const lang = getSelectValueFromModal();
            const published = isModalCheckboxChecked();

            makeAjaxRequest(HTTP_REQUEST_TYPE.PUT,
                "/api/games/" + gameId, {
                    name: name, description: desc, language: lang,
                    published: published
                }, updateGameCallback);
        });
    });
}

function updateGameCallback(response) {
    if (response.responseJSON.hasErrors === true) {
        showModalFieldErrors(response.responseJSON.fieldErrors);
    } else {
        hideModal();
    }
}

function createNode(parentId, node) {
    const createdNodeId = createJstreeNode(parentId, node);

    if (parentId === null) {
        parentId = '###';
    }

    makeAjaxRequest(HTTP_REQUEST_TYPE.POST,
        "/api/games/" + gameId + "/nodes/", {
            id: createdNodeId, parentId: parentId,
            name: (node.type !== NODE_TYPE.CONDITION ? node.text : null),
            description: node.data[DESC_INDEX], type: node.type,
            condition: (node.type !== NODE_TYPE.CONDITION ? undefined : {
                flagId: node.data[COND_INDEX].flagId,
                flagState: node.data[COND_INDEX].flagState
            })
        }, createNodeCallback(createdNodeId, node, parentId));
}

function createNodeCallback(createdNodeId, node, parentId) {
    return function (response) {
        if (response.responseJSON.hasErrors === true) {
            showModalFieldErrors(response.responseJSON.fieldErrors);

            deleteJstreeNode(createdNodeId);
        } else {
            if (node.type === NODE_TYPE.FLAG) {
                node.id = createdNodeId;

                flags.push(node);
            }
            if (node.type === NODE_TYPE.CONDITION) {
                const condition = node.data[COND_INDEX];

                condition.nodeId = createdNodeId;

                conditions.push(condition);
            }

            hideModal();
            openJstreeNode(parentId);
        }
    }
}

function renameNode(node, name, desc) {
    makeAjaxRequest(HTTP_REQUEST_TYPE.PUT, "/api/games/" + gameId +
        "/nodes/" + node.id, {
        name: name, description: desc, type: node.type,
        condition: node.data[COND_INDEX]
    }, renameNodeCallback(node, name, desc));
}

function renameNodeCallback(node, name, desc) {
    return function (response) {
        if (response.responseJSON.hasErrors === true) {
            showModalFieldErrors(response.responseJSON.fieldErrors);
        } else {
            renameJstreeNode(node, name);

            if (node.type === NODE_TYPE.ROOM) {
                // set the new description to the node in jstree
                getJstreeNode(node.id).data[DESC_INDEX] = desc;
            }

            if (node.type === NODE_TYPE.FLAG) {
                updateConditionsNameByFlag(node);
            }

            hideModal();
        }
    };
}

function deleteNode(node) {
    makeAjaxRequest(HTTP_REQUEST_TYPE.DELETE,
        "/api/games/" + gameId + "/nodes/" + node.id, null,
        deleteNodeCallback(node));
}

function deleteNodeCallback(node) {
    return function (response) {
        if (response.responseJSON === true) {
            deleteJstreeNode(node);

            if (node.type === NODE_TYPE.FLAG) {
                removeFromFlags(node);
                removeFromConditionsByFlag(node);
            }

        } else {
            showAlertModal(DELETE_ITEM_STRING, CAN_NOT_DELETE_ITEM_STRING,
                null, ALERT_MODE.INFO)
        }
    };
}

function getJstreeNode(id) {
    return gameTree.jstree().get_node(id);
}

function createJstreeNode(parentId, node) {
    return gameTree.jstree().create_node(parentId, node);
}

function renameJstreeNode(node, name) {
    gameTree.jstree().rename_node(node, name);
}

function deleteJstreeNode(id) {
    gameTree.jstree().delete_node(id);
}

function openJstreeNode(id) {
    gameTree.jstree().open_all(id);
}

function removeFromFlags(node) {
    flags.splice(flags.indexOf(
        flags.find(function (f) {
            return f.id = node.id;
        })), 1);
}

function removeFromConditionsByFlag(node) {
    for (let condition of conditions) {
        if (condition.flagId === node.id) {
            deleteJstreeNode(condition.nodeId);

            conditions.splice(conditions.indexOf(condition), 1);
        }
    }
}

function updateConditionsNameByFlag(node) {
    for (let conditionNode of conditions) {
        if (conditionNode.flagId === node.id) {
            const nodeToRename = getJstreeNode(
                conditionNode.nodeId);

            const newConditionName = createConditionName(
                getJstreeNode(conditionNode.flagId),
                conditionNode.flagState);

            renameJstreeNode(nodeToRename, newConditionName);
        }
    }
}

function hasRoomAmongChildren(node) {
    for (let childId of node.children) {
        const childNode = getJstreeNode(childId);

        if (childNode.type === NODE_TYPE.ROOM) {
            return true;
        }
    }

    return false;
}

try {
    module.exports = {
        initGameEditor, showCreateGameModal, showUpdateGameInfoModal
    }
} catch (e) {
    if (e instanceof ReferenceError) {
        /*
        There is no need to do something, module exports are only needed in
        nodejs unit tests.
        */
    } else throw e;
}