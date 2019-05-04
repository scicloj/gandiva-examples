(ns gandiva-examples.core
  (:import org.apache.arrow.gandiva.evaluator.Projector
           (org.apache.arrow.vector.types.pojo Field Schema
                                               ArrowType ArrowType$Int ArrowType$Bool)
           org.apache.arrow.vector.types.FloatingPointPrecision
           (org.apache.arrow.gandiva.evaluator Projector)
           (org.apache.arrow.gandiva.expression TreeBuilder ExpressionTree)
           com.google.common.collect.Lists
           (org.apache.arrow.vector.ipc.message ArrowRecordBatch ArrowFieldNode)
           io.netty.buffer.ArrowBuf
           (org.apache.arrow.memory BufferAllocator RootAllocator)
           (org.apache.arrow.vector IntVector)
           (java.util Arrays List ArrayList)))

;; Inspired by:
;; https://github.com/apache/arrow/blob/master/java/gandiva/src/test/java/org/apache/arrow/gandiva/evaluator/BaseEvaluatorTest.java
;; https://github.com/apache/arrow/blob/master/java/gandiva/src/test/java/org/apache/arrow/gandiva/evaluator/ProjectorTest.java


(set! *warn-on-reflection* true)

(def int32 (ArrowType$Int. 64 true))
(def int64 (ArrowType$Int. 64 true))
(def bool (ArrowType$Bool.))

(defn ->int64-field [field-name]
  (-> field-name
      name
      (Field/nullable int64)))

(defn ->int32-field [field-name]
  (-> field-name
      name
      (Field/nullable int32)))

(def allocator
  (RootAllocator. Long/MAX_VALUE))

(defn buf [^bytes bytes]
  (let [buffer ^ArrowBuf (.buffer ^BufferAllocator allocator (count bytes))]
    (.writeBytes buffer bytes)
    buffer))

(defn int-buf [^ints ints]
  (let [buffer ^ArrowBuf (.buffer ^BufferAllocator allocator (* 4 (count ints)))]
    (dotimes [i (count ints)]
      (.writeInt buffer (aget ints i)))
    buffer))

(def empty-schema-path "")

(defn into-list [xs]
  (Arrays/asList (into-array xs)))

(defn evalutate-example []
  ;; Inspired by: ;; https://github.com/apache/arrow/blob/master/java/gandiva/src/test/java/org/apache/arrow/gandiva/evaluator/ProjectorTest.java#L212
  (let [[a b c]       (->> [:a :b :c]
                           (map ->int32-field))
        a-node        (TreeBuilder/makeField a)
        b-node        (TreeBuilder/makeField b)
        expr          (TreeBuilder/makeExpression "add" [a b] c)
        schema        (Schema. [a b])
        projector     ^Projector (Projector/make schema [expr])
        num-rows      16
        a-values      (int-array (range num-rows))
        b-values      (int-array (map (partial * 100) (range num-rows)))
        ;; bitmaps marking that all values are no-null:
        ;; (see https://arrow.apache.org/docs/format/Layout.html#null-bitmaps)
        a-validity    (byte-array [255 255])
        b-validity    (byte-array [255 255])
        output-vector (IntVector. empty-schema-path allocator)
        _             (.allocateNew output-vector  (* 4 num-rows))
        output        [output-vector]]
    (.evaluate projector
               num-rows
               ^List (into-list [(buf a-validity) (int-buf a-values)
                                 (buf b-validity) (int-buf b-values)])
               ^List (into-list [output-vector]))
    ;; TODO: Figure out how to release output-vector.
    (->> num-rows
         range
         (map #(.get output-vector %)))))


(comment
  (evalutate-example))
