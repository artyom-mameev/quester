function getNextReachableRoom(choice, activeFlags) {
    let nextReachableRoom;

    const conditions = [];

    for (let choiceChild of choice.children) {
        if (choiceChild.type === NODE_TYPE.ROOM) {
            nextReachableRoom = choiceChild;
        }
        if (choiceChild.type === NODE_TYPE.CONDITION) {
            conditions.push(choiceChild);
        }
    }

    // check conditions
    for (let conditionNode of conditions) {
        /*if conditionNode's flag condition is active and that flag
        is triggered*/
        if (conditionNode.condition.flagState === FLAG_STATE.FLAG_ACTIVE &&
            activeFlags.includes(conditionNode.condition.flagId) ||
            /*or if conditionNode's flag condition is not active
            and that flag is triggered (is not present in the list of flags with
            active state)*/
            conditionNode.condition.flagState === FLAG_STATE.FLAG_NOT_ACTIVE &&
            !activeFlags.includes(conditionNode.condition.flagId)) {
            // check if there is a reachable room among conditionNode's children
            const nextRoomFromSucceedCondition = getNextReachableRoom(
                conditionNode, activeFlags);
            // and if it is
            if (nextRoomFromSucceedCondition !== undefined) {
                /*return that room as next reachable room, even if reachable room
                already exists (room from succeed condition has priority
                over the normal reachable room)*/
                return nextRoomFromSucceedCondition;
            }
        }
    }
    /*if there is no conditions, or conditions are not succeed, return next
    (normal, default) room*/
    return nextReachableRoom;
}

try {
    module.exports = {
        getNextReachableRoom
    };
} catch (e) {
    if (e instanceof ReferenceError) {
        /*
        There is no need to do something, module exports are only needed in
        nodejs unit tests.
        */
    } else throw e;
}