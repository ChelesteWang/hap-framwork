/*
 * Copyright (c) 2021, the hapjs-platform Project Contributors
 *
 * SPDX-License-Identifier: Apache-2.0
 */

function makeTimer(inst, callback, normalize) {
  const _inst = inst
  const _timerCallbackMap = {}

  return {
    setTimeout: function(cb, time) {
      const cid = normalize(cb, _inst)
      const handler = function() {
        callback(_inst, cid)
      }
      const tid = global.setTimeoutWrap(_inst.id, handler, time || 0)
      _timerCallbackMap[tid.toString()] = cid
      return tid.toString()
    },
    setInterval: function(cb, time) {
      const cid = normalize(cb, _inst)
      const handler = function() {
        callback(_inst, cid, [], true)
      }
      const tid = global.setIntervalWrap(_inst.id, handler, time || 0)
      _timerCallbackMap[tid.toString()] = cid
      return tid.toString()
    },
    clearTimeout: function(n) {
      global.clearTimeoutWrap(n)
      _timerCallbackMap[n] = undefined
    },
    clearInterval: function(n) {
      global.clearIntervalWrap(n)
      _timerCallbackMap[n] = undefined
    },
    requestAnimationFrame: function(cb) {
      const cid = normalize(cb, _inst)
      const handler = function() {
        callback(_inst, cid)
      }
      const tid = global.requestAnimationFrameWrap(_inst.id, handler)
      _timerCallbackMap[tid.toString()] = cid
      return tid.toString()
    },
    cancelAnimationFrame: function(n) {
      global.cancelAnimationFrameWrap(n)
      _timerCallbackMap[n] = undefined
    }
  }
}

export { makeTimer }
