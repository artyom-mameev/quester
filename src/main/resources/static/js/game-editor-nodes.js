function convertToJstreeNodesFormat(node) {
    node.text = node.name;
    node.data = [];

    if (node.type === NODE_TYPE.ROOM) {
        node.data[DESC_INDEX] = node.description;
    }

    if (node.type === NODE_TYPE.CONDITION) {
        node.data[COND_INDEX] = node.condition;
    }

    delete node.name;
    delete node.description;
    delete node.condition;

    for (let child of node.children) {
        convertToJstreeNodesFormat(child);
    }
}

function createNamesForConditionNodes(node, rootNode) {
    if (rootNode === undefined) {
        rootNode = node;
    }

    if (node.type === NODE_TYPE.CONDITION) {
        const flagNode = findGameNodeById(rootNode, node.data[COND_INDEX].flagId);
        const flagState = node.data[COND_INDEX].flagState;

        node.text = createConditionName(flagNode, flagState);
    }

    for (let child of node.children) {
        createNamesForConditionNodes(child, rootNode);
    }
}

function createConditionName(flagNode, flagState) {
    let conditionName = IF_STRING + " " + flagNode.text;

    if (flagState === FLAG_STATE.FLAG_ACTIVE) {
        conditionName += " " + IS_ACTIVE_STRING;
    }
    if (flagState === FLAG_STATE.FLAG_NOT_ACTIVE) {
        conditionName += " " + IS_NOT_ACTIVE_STRING;
    }

    return conditionName;
}

function getAllFlags(node) {
    let flags = [];

    if (node.type === NODE_TYPE.FLAG) {
        flags.push(node);
    }

    for (let child of node.children) {
        flags.push(...getAllFlags(child));
    }

    return flags;
}

function getAllConditions(node) {
    let conditions = [];

    if (node.type === NODE_TYPE.CONDITION) {
        conditions.push(node.data[COND_INDEX]);
    }

    for (let child of node.children) {
        conditions.push(...getAllConditions(child));
    }

    return conditions;
}

function findGameNodeById(rootNode, id) {
    if (rootNode.id === id) {
        return rootNode;
    } else {
        for (let child of rootNode.children) {
            const found = findGameNodeById(child, id);

            if (found !== null) {
                return found;
            }
        }

        return null;
    }
}

class GameNode {
    data = [];

    constructor(name, description, type) {
        this.text = name;
        this.data.push(description);
        this.type = type;
    }
}

class Condition {
    constructor(flagId, flagState) {
        this.flagId = flagId;
        this.flagState = flagState;
    }
}

try {
    module.exports = {
        GameNode, Condition, convertToJstreeNodesFormat,
        createNamesForConditionNodes, createConditionName, getAllFlags,
        getAllConditions
    }
} catch (e) {
    if (e instanceof ReferenceError) {
        /*
        There is no need to do something, module exports are only needed in
        nodejs unit tests.
        */
    } else throw e;
}
