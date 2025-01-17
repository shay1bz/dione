package com.paypal.dione.spark.index.parquet

import com.databricks.spark.avro.SchemaConverters
import com.paypal.dione.avro.utils.GenericRecordMap
import com.paypal.dione.hdfs.index.HdfsIndexer
import com.paypal.dione.hdfs.index.parquet.ParquetIndexer
import com.paypal.dione.spark.index.{IndexManagerFactory, IndexSpec, SparkIndexer}
import org.apache.avro.generic.GenericRecord
import org.apache.avro.{Schema, SchemaBuilder}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.StructType


object ParquetSparkIndexer extends IndexManagerFactory {

  override def canResolve(inputFormat: String, serde: String): Boolean =
    inputFormat.contains("Parquet") && serde.contains("parquet.serde")

  override def createSparkIndexer(spark: SparkSession, indexSpec: IndexSpec): SparkIndexer =
    ParquetSparkIndexer(spark)
}

case class ParquetSparkIndexer(@transient spark: SparkSession) extends SparkIndexer {

  override type T = GenericRecord

  def initHdfsIndexer(file: Path, conf: Configuration, start: Long, end: Long, fieldsSchema: StructType): HdfsIndexer[GenericRecord] = {
    ParquetIndexer(file, start, end, conf, Some(getProjectedAvroSchema(fieldsSchema)))
  }

  def convert(gr: GenericRecord): Seq[Any] =
    (0 until gr.getSchema.getFields.size()).map(gr.get)

  def convertMap(gr: GenericRecord): Map[String, Any] =
    GenericRecordMap(gr)

  private def getProjectedAvroSchema(structType: StructType): Schema = {
    val builder = SchemaBuilder.record("asd")
    SchemaConverters.convertStructToAvro(structType, builder, "dsa_ns")
  }

}
