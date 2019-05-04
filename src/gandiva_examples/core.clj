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

(def int32 (ArrowType$Int. 32 true))
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

(defn buf [bytes]
  (let [buffer ^ArrowBuf (.buffer ^BufferAllocator allocator
                                  (count bytes))]
    (.writeBytes buffer (byte-array bytes))
    buffer))


(defn int-buf [ints]
  (let [buffer ^ArrowBuf (.buffer ^BufferAllocator allocator
                                  ;; One int is 4 bytes.
                                  (* 4 (count ints)))]
    (doseq [^int x ints]
      (.writeInt buffer x))
    buffer))

(def empty-schema-path "")

(defn into-list [xs]
  ^List (Arrays/asList (into-array xs)))

(defn evaluate-example []
  ;; Inspired by: ;; https://github.com/apache/arrow/blob/master/java/gandiva/src/test/java/org/apache/arrow/gandiva/evaluator/ProjectorTest.java#L212
  (let [;; Types of the involved fields:
        [a b c]    (map ->int32-field [:a :b :c])
        ;; Making the expression to compute c=a+b:
        expr       (TreeBuilder/makeExpression "add" [a b] c) ; c=a+b
        ;; Defining the Schema - https://arrow.apache.org/docs/metadata.html:
        schema     (Schema. [a b])
        ;; The projector will evaluate the expression:
        projector  ^Projector (Projector/make schema [expr])
        ;; Now for the actual data: 16 rows, 8 actually ignored.
        num-rows   16
        null-count 8
        a-values   (int-array (range num-rows))
        b-values   (int-array (map (partial * 100) (range num-rows)))
        ;; We use these bitmaps to declare that half of the values are null:
        ;; (see https://arrow.apache.org/docs/format/Layout.html#null-bitmaps)
        a-validity (byte-array [255 0])
        b-validity (byte-array [255 0])
        ;; Creating the record batch for holding the data:
        ;; See https://wesmckinney.com/blog/arrow-streaming-columnar/
        batch      (ArrowRecordBatch.
                    ;; number of rows
                    num-rows
                    ;; field nodes:
                    (into-list [(ArrowFieldNode. num-rows null-count)
                                (ArrowFieldNode. num-rows null-count)])
                    ;; data buffers:
                    (into-list [(buf a-validity) (int-buf a-values)
                                (buf b-validity) (int-buf b-values)])) 
        ;; Allocating the output:
        int-vector (IntVector. empty-schema-path allocator)
        ;; An int is 4 bytes:
        _          (.allocateNew int-vector   (* 4 num-rows))
        ;; Evaluating the expression:
        _          (.evaluate projector
                              batch
                              (into-list [int-vector]))
        ;; Translating to a clojure data structure:
        result     (->> (- num-rows null-count)
                        range
                        (mapv #(.get int-vector %)))]
    ;; Releasing memory:
    (doseq [^ArrowBuf buf (.getBuffers batch)]
      (.release buf))
    (.close batch)
    (.close int-vector)
    ;; Returning:
    result))

(comment
  (evaluate-example)
  #_=> [0 101 202 303 404 505 606 707])
